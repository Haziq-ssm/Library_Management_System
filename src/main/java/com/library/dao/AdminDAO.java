package com.library.dao;

import com.library.config.DBConnection;
import com.library.model.Admin;
import com.library.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminDAO {

    public Admin findByUsername(String username) throws SQLException {
        String sql = "SELECT admin_id, username, password_hash, full_name, email, created_at FROM admin WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                            rs.getInt("admin_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    public boolean createAdmin(Admin admin) throws SQLException {
        String sql = "INSERT INTO admin (username, password_hash, full_name, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPasswordHash());
            stmt.setString(3, admin.getFullName());
            stmt.setString(4, admin.getEmail());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        admin.setAdminId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updatePassword(int adminId, String newHashedPassword) throws SQLException {
        String sql = "UPDATE admin SET password_hash = ? WHERE admin_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, adminId);
            return stmt.executeUpdate() > 0;
        }
    }

    public void ensureDefaultAdminExists() {
        try {
            Admin admin = findByUsername("admin");
            if (admin == null) {
                String hash = PasswordUtil.hashPassword("admin123");
                createAdmin(new Admin("admin", hash, "System Administrator", "admin@library.com"));
                System.out.println("Default admin user created (admin / admin123).");
            } else if ("$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6.SfZV4Io4TU3L6C".equals(admin.getPasswordHash())) {
                // Older sample-data bundles shipped with a BCrypt hash that did not
                // match the documented starter password. Repair only that exact
                // known-bad seed hash; never overwrite a password chosen by an admin.
                updatePassword(admin.getAdminId(), PasswordUtil.hashPassword("admin123"));
                System.out.println("Repaired legacy starter-admin credentials.");
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not verify default admin: " + e.getMessage());
        }
    }
}
