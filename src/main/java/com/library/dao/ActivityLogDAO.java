package com.library.dao;

import com.library.config.DBConnection;
import com.library.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    public boolean logAction(String adminUsername, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (admin_username, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, adminUsername != null ? adminUsername : "admin");
            stmt.setString(2, actionType);
            stmt.setString(3, description);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Could not write activity log: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getRecentLogs(int limit) throws SQLException {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT log_id, admin_username, action_type, description, created_at " +
                     "FROM activity_logs ORDER BY log_id DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit > 0 ? limit : 10);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ActivityLog(
                            rs.getInt("log_id"),
                            rs.getString("admin_username"),
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return list;
    }
}
