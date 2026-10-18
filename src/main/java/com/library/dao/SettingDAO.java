package com.library.dao;

import com.library.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SettingDAO {

    public Map<String, String> getAllSettings() throws SQLException {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM settings";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        }
        return settings;
    }

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load setting '" + key + "': " + e.getMessage());
        }
        return defaultValue;
    }

    public boolean updateSetting(String key, String value) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection()) {
            boolean exists;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, key);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    exists = rs.next() && rs.getInt(1) > 0;
                }
            }

            if (exists) {
                String updateSql = "UPDATE settings SET setting_value = ? WHERE setting_key = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, value);
                    updateStmt.setString(2, key);
                    return updateStmt.executeUpdate() > 0;
                }
            } else {
                String insertSql = "INSERT INTO settings (setting_key, setting_value, description) VALUES (?, ?, '')";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, key);
                    insertStmt.setString(2, value);
                    return insertStmt.executeUpdate() > 0;
                }
            }
        }
    }

    public int getIntSetting(String key, int defaultValue) {
        String val = getSetting(key, null);
        if (val != null) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public double getDoubleSetting(String key, double defaultValue) {
        String val = getSetting(key, null);
        if (val != null) {
            try {
                return Double.parseDouble(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
