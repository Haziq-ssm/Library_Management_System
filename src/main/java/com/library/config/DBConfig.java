package com.library.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Manages database configuration loaded from properties file, environment variables,
 * or runtime settings.
 */
public class DBConfig {
    private static final String DEFAULT_PROPERTIES_FILE = "db.properties";

    private String driver;
    private String url;
    private String user;
    private String password;
    private boolean fallbackEnabled;

    private static DBConfig instance;

    private DBConfig() {
        loadConfiguration();
    }

    public static synchronized DBConfig getInstance() {
        if (instance == null) {
            instance = new DBConfig();
        }
        return instance;
    }

    public void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load " + DEFAULT_PROPERTIES_FILE + ", using defaults: " + e.getMessage());
        }

        // Support environment variables overriding config
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        this.url = envUrl != null ? envUrl : props.getProperty("db.url", "jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        this.user = envUser != null ? envUser : props.getProperty("db.user", "root");
        this.password = envPass != null ? envPass : props.getProperty("db.password", "");
        this.fallbackEnabled = Boolean.parseBoolean(props.getProperty("db.fallback.enabled", "true"));
    }

    public void updateConnectionSettings(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }
}
