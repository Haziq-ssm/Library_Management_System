package com.library.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reusable JDBC Connection manager.
 * Connects directly to MySQL. If MySQL is not currently reachable and fallback is enabled,
 * it safely initialises a MySQL-compatible local embedded engine with sample data
 * so the application remains immediately responsive and testable without crash.
 */
public class DBConnection {

    private static boolean usingFallback = false;
    private static String lastErrorMessage = null;
    private static boolean fallbackInitialized = false;

    // Local embedded database URL configured in MySQL compatibility mode
    private static final String FALLBACK_URL = "jdbc:h2:file:./library_db_local;MODE=MySQL;DATABASE_TO_UPPER=FALSE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
    private static final String FALLBACK_USER = "sa";
    private static final String FALLBACK_PASSWORD = "";

    /**
     * Obtains a live JDBC Connection.
     * Always attempts MySQL first.
     */
    public static Connection getConnection() throws SQLException {
        DBConfig config = DBConfig.getInstance();

        if (!usingFallback) {
            try {
                Class.forName(config.getDriver());
                Connection conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
                lastErrorMessage = null;
                return conn;
            } catch (ClassNotFoundException e) {
                lastErrorMessage = "MySQL JDBC Driver not found: " + e.getMessage();
                System.err.println(lastErrorMessage);
            } catch (SQLException e) {
                lastErrorMessage = "Could not connect to MySQL: " + e.getMessage();
                System.err.println(lastErrorMessage);

                if (config.isFallbackEnabled()) {
                    System.out.println("Switching to MySQL-compatible local fallback mode...");
                    usingFallback = true;
                } else {
                    throw e;
                }
            }
        }

        // Fallback connection mode
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(FALLBACK_URL, FALLBACK_USER, FALLBACK_PASSWORD);
            if (!fallbackInitialized) {
                initializeFallbackDatabase(conn);
                fallbackInitialized = true;
            }
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Neither MySQL nor embedded database driver is available: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes the fallback database schema and sample data if tables don't exist yet.
     */
    private static synchronized void initializeFallbackDatabase(Connection conn) {
        try {
            // Check if tables already exist
            try (Statement checkStmt = conn.createStatement()) {
                checkStmt.executeQuery("SELECT 1 FROM books LIMIT 1");
                System.out.println("Fallback database already initialized.");
                return;
            } catch (SQLException ignored) {
                // Table doesn't exist yet, proceed with initialization
            }

            System.out.println("Initializing local fallback schema and sample data...");
            runSqlScriptFromClasspath(conn, "database/schema.sql");
            runSqlScriptFromClasspath(conn, "database/sample_data.sql");
            System.out.println("Fallback database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Warning: Error initializing fallback database: " + e.getMessage());
        }
    }

    /**
     * Executes an SQL script from a file or classpath resource.
     */
    public static void runSqlScriptFromClasspath(Connection conn, String resourcePath) {
        InputStream in = DBConnection.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            // Try loading from relative file path
            java.io.File file = new java.io.File(resourcePath);
            if (file.exists()) {
                try {
                    in = new java.io.FileInputStream(file);
                } catch (Exception e) {
                    System.err.println("Could not open file: " + resourcePath);
                    return;
                }
            } else {
                System.err.println("SQL resource not found: " + resourcePath);
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--") || line.startsWith("//") || line.isEmpty()) {
                    continue;
                }
                // Skip MySQL specific DB creation statements in fallback mode
                if (usingFallback && (line.toUpperCase().startsWith("CREATE DATABASE") || line.toUpperCase().startsWith("USE "))) {
                    continue;
                }
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    // Strip trailing semicolon
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException ex) {
                            // Suppress drop table warnings or minor schema syntax differences
                            if (!sql.toUpperCase().startsWith("DROP TABLE")) {
                                System.err.println("SQL Notice on [" + sql + "]: " + ex.getMessage());
                            }
                        }
                    }
                    sb.setLength(0);
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing SQL script " + resourcePath + ": " + e.getMessage());
        }
    }

    /**
     * Tests a specific MySQL connection configuration.
     */
    public static boolean testConnection(String url, String user, String password) throws SQLException, ClassNotFoundException {
        Class.forName(DBConfig.getInstance().getDriver());
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            return conn.isValid(3);
        }
    }

    public static boolean isFallbackMode() {
        return usingFallback;
    }

    public static void setUsingFallback(boolean fallback) {
        usingFallback = fallback;
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
