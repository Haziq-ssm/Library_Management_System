package com.library;

import com.formdev.flatlaf.FlatLightLaf;
import com.library.config.DBConnection;
import com.library.dao.AdminDAO;
import com.library.ui.LoginFrame;

import javax.swing.*;
import java.sql.Connection;

/**
 * Main application entry point for the Library Management System.
 */
public class Main {

    public static void main(String[] args) {
        // Configure high-DPI scaling and font rendering
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize Modern FlatLaf Look and Feel
        try {
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Notice: FlatLaf not found, using System Look and Feel: " + e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Test database connection asynchronously on startup
        new Thread(() -> {
            try {
                Connection conn = DBConnection.getConnection();
                if (conn != null && !conn.isClosed()) {
                    System.out.println("Database connection established successfully. Fallback mode: " + DBConnection.isFallbackMode());
                    new AdminDAO().ensureDefaultAdminExists();
                    conn.close();
                }
            } catch (Exception e) {
                System.err.println("Startup database check notice: " + e.getMessage());
            }
        }).start();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
