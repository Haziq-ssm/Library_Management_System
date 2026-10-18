package com.library.service;

import com.library.dao.SettingDAO;

import java.sql.SQLException;
import java.util.Map;

public class SettingService {

    private final SettingDAO settingDAO;

    public SettingService() {
        this.settingDAO = new SettingDAO();
    }

    public Map<String, String> getAllSettings() throws SQLException {
        return settingDAO.getAllSettings();
    }

    public String getSetting(String key, String defaultValue) {
        return settingDAO.getSetting(key, defaultValue);
    }

    public boolean updateSetting(String key, String value) throws SQLException {
        return settingDAO.updateSetting(key, value);
    }

    public int getLoanPeriodDays() {
        return settingDAO.getIntSetting("loan_period_days", 14);
    }

    public double getFinePerDay() {
        return settingDAO.getDoubleSetting("fine_per_day", 5.00);
    }

    public int getMaxBooksPerMember() {
        return settingDAO.getIntSetting("max_books_per_member", 5);
    }

    public String getLibraryName() {
        return settingDAO.getSetting("library_name", "Library System");
    }

    public String getCurrencySymbol() {
        return settingDAO.getSetting("currency_symbol", "₹");
    }
}
