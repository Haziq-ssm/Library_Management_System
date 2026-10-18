package com.library.ui.panels;

import com.library.config.DBConfig;
import com.library.config.DBConnection;
import com.library.service.AuthService;
import com.library.service.SettingService;
import com.library.ui.DashboardFrame;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedPanel;
import com.library.ui.components.RoundedPasswordField;
import com.library.ui.components.RoundedTextField;
import com.library.ui.theme.ThemeColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final SettingService settingService;
    private final AuthService authService;

    // Library Rule Inputs
    private RoundedTextField txtLibName;
    private RoundedTextField txtLoanDays;
    private RoundedTextField txtFineRate;
    private RoundedTextField txtMaxBooks;
    private RoundedTextField txtCurrency;

    // Password Inputs
    private RoundedPasswordField txtOldPassword;
    private RoundedPasswordField txtNewPassword;
    private RoundedPasswordField txtConfirmPassword;

    // Database Connection Inputs
    private RoundedTextField txtDbUrl;
    private RoundedTextField txtDbUser;
    private RoundedPasswordField txtDbPass;
    private JLabel lblDbStatus;

    public SettingsPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.settingService = new SettingService();
        this.authService = new AuthService();

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadCurrentSettings();
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        // Header Title
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Settings");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Configure library policies, security credentials, and MySQL connectivity");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(lblSub);
        scrollContent.add(headerPanel);
        scrollContent.add(Box.createVerticalStrut(20));

        // Two Column Grid for Settings Sections
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);

        // Left Column: Business Rules & Admin Security
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        leftCol.add(createLibraryRulesCard());
        leftCol.add(Box.createVerticalStrut(20));
        leftCol.add(createSecurityCard());

        grid.add(leftCol);

        // Right Column: Database Connectivity Card
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        rightCol.add(createDatabaseConfigCard());
        grid.add(rightCol);

        scrollContent.add(grid);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new com.library.ui.components.CustomScrollBarUI());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private RoundedPanel createLibraryRulesCard() {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE, ThemeColors.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Library Policies & Borrowing Rules");
        title.setFont(ThemeColors.FONT_CARD_TITLE);
        title.setForeground(ThemeColors.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 0, 0, 0));

        txtLibName = new RoundedTextField(20);
        form.add(createFieldWrapper("Library Display Name", txtLibName));
        form.add(Box.createVerticalStrut(10));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 12, 0));
        row1.setOpaque(false);
        txtLoanDays = new RoundedTextField(8);
        txtFineRate = new RoundedTextField(8);
        row1.add(createFieldWrapper("Default Loan Period (Days)", txtLoanDays));
        row1.add(createFieldWrapper("Fine per Overdue Day (₹)", txtFineRate));
        form.add(row1);
        form.add(Box.createVerticalStrut(10));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        txtMaxBooks = new RoundedTextField(8);
        txtCurrency = new RoundedTextField(8);
        row2.add(createFieldWrapper("Max Books Borrowed per Member", txtMaxBooks));
        row2.add(createFieldWrapper("Currency Symbol", txtCurrency));
        form.add(row2);
        form.add(Box.createVerticalStrut(16));

        RoundedButton btnSaveRules = new RoundedButton("Save Policies");
        btnSaveRules.setPreferredSize(new Dimension(130, 36));
        btnSaveRules.addActionListener(e -> saveLibraryRules());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnSaveRules);
        form.add(btnRow);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel createSecurityCard() {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE, ThemeColors.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Change Admin Password");
        title.setFont(ThemeColors.FONT_CARD_TITLE);
        title.setForeground(ThemeColors.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 0, 0, 0));

        txtOldPassword = new RoundedPasswordField("Enter current password", 20);
        form.add(createFieldWrapper("Current Password *", txtOldPassword));
        form.add(Box.createVerticalStrut(10));

        txtNewPassword = new RoundedPasswordField("Enter new password (min 4 chars)", 20);
        form.add(createFieldWrapper("New Password *", txtNewPassword));
        form.add(Box.createVerticalStrut(10));

        txtConfirmPassword = new RoundedPasswordField("Re-enter new password", 20);
        form.add(createFieldWrapper("Confirm New Password *", txtConfirmPassword));
        form.add(Box.createVerticalStrut(16));

        RoundedButton btnChangePass = new RoundedButton("Update Password");
        btnChangePass.setPreferredSize(new Dimension(150, 36));
        btnChangePass.addActionListener(e -> changeAdminPassword());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnChangePass);
        form.add(btnRow);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel createDatabaseConfigCard() {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE, ThemeColors.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("MySQL Database Connectivity");
        title.setFont(ThemeColors.FONT_CARD_TITLE);
        title.setForeground(ThemeColors.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 0, 0, 0));

        // Connection Status Banner
        RoundedPanel statusBanner = new RoundedPanel(10, ThemeColors.BG_INPUT, ThemeColors.BORDER);
        statusBanner.setLayout(new BorderLayout());
        statusBanner.setBorder(new EmptyBorder(10, 14, 10, 14));

        lblDbStatus = new JLabel(DBConnection.isFallbackMode() ?
                "● Status: Local Mode (MySQL not connected)" :
                "● Status: Connected to MySQL (library_db)");
        lblDbStatus.setFont(ThemeColors.FONT_BODY_BOLD);
        lblDbStatus.setForeground(DBConnection.isFallbackMode() ? ThemeColors.BADGE_WARNING_FG : ThemeColors.BADGE_SUCCESS_FG);
        statusBanner.add(lblDbStatus, BorderLayout.CENTER);
        form.add(statusBanner);
        form.add(Box.createVerticalStrut(14));

        txtDbUrl = new RoundedTextField(20);
        txtDbUrl.setText(DBConfig.getInstance().getUrl());
        form.add(createFieldWrapper("JDBC Connection URL", txtDbUrl));
        form.add(Box.createVerticalStrut(10));

        txtDbUser = new RoundedTextField(20);
        txtDbUser.setText(DBConfig.getInstance().getUser());
        form.add(createFieldWrapper("Database User", txtDbUser));
        form.add(Box.createVerticalStrut(10));

        txtDbPass = new RoundedPasswordField("MySQL password (leave blank if none)", 20);
        txtDbPass.setText(DBConfig.getInstance().getPassword());
        form.add(createFieldWrapper("Database Password", txtDbPass));
        form.add(Box.createVerticalStrut(18));

        // Action Buttons Row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        RoundedButton btnTest = new RoundedButton("Test Connection", true);
        btnTest.setPreferredSize(new Dimension(140, 36));
        btnTest.addActionListener(e -> testDbConnection());
        btnRow.add(btnTest);

        RoundedButton btnSaveDb = new RoundedButton("Save & Connect");
        btnSaveDb.setPreferredSize(new Dimension(140, 36));
        btnSaveDb.addActionListener(e -> saveAndReconnectDb());
        btnRow.add(btnSaveDb);

        form.add(btnRow);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFieldWrapper(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeColors.FONT_BODY_BOLD);
        lbl.setForeground(ThemeColors.TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void loadCurrentSettings() {
        txtLibName.setText(settingService.getLibraryName());
        txtLoanDays.setText(String.valueOf(settingService.getLoanPeriodDays()));
        txtFineRate.setText(String.format("%.2f", settingService.getFinePerDay()));
        txtMaxBooks.setText(String.valueOf(settingService.getMaxBooksPerMember()));
        txtCurrency.setText(settingService.getCurrencySymbol());
    }

    private void saveLibraryRules() {
        try {
            String name = txtLibName.getText().trim();
            int days = Integer.parseInt(txtLoanDays.getText().trim());
            double fine = Double.parseDouble(txtFineRate.getText().trim());
            int maxBooks = Integer.parseInt(txtMaxBooks.getText().trim());
            String cur = txtCurrency.getText().trim();

            settingService.updateSetting("library_name", name);
            settingService.updateSetting("loan_period_days", String.valueOf(days));
            settingService.updateSetting("fine_per_day", String.format("%.2f", fine));
            settingService.updateSetting("max_books_per_member", String.valueOf(maxBooks));
            settingService.updateSetting("currency_symbol", cur);

            JOptionPane.showMessageDialog(this, "Library policies saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for loan period, fine, and max books.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeAdminPassword() {
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Current and new password are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int adminId = AuthService.getCurrentLoggedInAdmin() != null ? AuthService.getCurrentLoggedInAdmin().getAdminId() : 1;
            authService.changePassword(adminId, oldPass, newPass);
            JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            txtOldPassword.setText("");
            txtNewPassword.setText("");
            txtConfirmPassword.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Password Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void testDbConnection() {
        String url = txtDbUrl.getText().trim();
        String user = txtDbUser.getText().trim();
        String pass = new String(txtDbPass.getPassword());

        try {
            boolean valid = DBConnection.testConnection(url, user, pass);
            if (valid) {
                JOptionPane.showMessageDialog(this, "Connection successful! MySQL is online and accessible.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Connection failed: Invalid response from server.", "Test Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAndReconnectDb() {
        String url = txtDbUrl.getText().trim();
        String user = txtDbUser.getText().trim();
        String pass = new String(txtDbPass.getPassword());

        DBConfig.getInstance().updateConnectionSettings(url, user, pass);
        DBConnection.setUsingFallback(false);

        testDbConnection();
        lblDbStatus.setText(DBConnection.isFallbackMode() ?
                "● Status: Local Mode (MySQL not connected)" :
                "● Status: Connected to MySQL (library_db)");
        lblDbStatus.setForeground(DBConnection.isFallbackMode() ? ThemeColors.BADGE_WARNING_FG : ThemeColors.BADGE_SUCCESS_FG);
        parentFrame.refreshDashboardStats();
    }
}
