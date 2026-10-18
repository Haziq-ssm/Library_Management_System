package com.library.service;

import com.library.dao.AdminDAO;
import com.library.model.Admin;
import com.library.util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {

    private final AdminDAO adminDAO;
    private static Admin currentLoggedInAdmin;

    public AuthService() {
        this.adminDAO = new AdminDAO();
    }

    public Admin authenticate(String username, String plainPassword) throws SQLException {
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Username and password must not be empty.");
        }

        Admin admin = adminDAO.findByUsername(username.trim());
        if (admin == null) {
            return null; // Username not found
        }

        if (PasswordUtil.checkPassword(plainPassword, admin.getPasswordHash())) {
            currentLoggedInAdmin = admin;
            return admin;
        }

        return null; // Invalid password
    }

    public boolean changePassword(int adminId, String oldPassword, String newPassword) throws SQLException {
        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("New password must be at least 4 characters long.");
        }

        Admin admin = currentLoggedInAdmin;
        if (admin == null || admin.getAdminId() != adminId) {
            admin = adminDAO.findByUsername(admin != null ? admin.getUsername() : "admin");
        }

        if (admin != null && PasswordUtil.checkPassword(oldPassword, admin.getPasswordHash())) {
            String newHash = PasswordUtil.hashPassword(newPassword);
            boolean updated = adminDAO.updatePassword(adminId, newHash);
            if (updated) {
                admin.setPasswordHash(newHash);
            }
            return updated;
        }

        throw new IllegalArgumentException("Incorrect current password.");
    }

    public static Admin getCurrentLoggedInAdmin() {
        return currentLoggedInAdmin;
    }

    public static void setCurrentLoggedInAdmin(Admin admin) {
        currentLoggedInAdmin = admin;
    }

    public static void logout() {
        currentLoggedInAdmin = null;
    }
}
