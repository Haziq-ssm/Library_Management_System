package com.library.ui;

import com.library.dao.AdminDAO;
import com.library.model.Admin;
import com.library.service.AuthService;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedPasswordField;
import com.library.ui.components.RoundedTextField;
import com.library.ui.theme.ThemeColors;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {

    private final AuthService authService;
    private RoundedTextField txtUsername;
    private RoundedPasswordField txtPassword;
    private JCheckBox chkRemember;
    private JLabel lblError;

    public LoginFrame() {
        super("Library Management System - Login");
        this.authService = new AuthService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 620);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Color.WHITE);

        // 1. Left Column: Branding & Illustration
        root.add(createLeftBrandingPanel());

        // 2. Right Column: Login Card
        root.add(createRightLoginPanel());

        setContentPane(root);
    }

    private JPanel createLeftBrandingPanel() {
        JPanel left = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Warm gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 248, 242), 0, h, new Color(254, 237, 222));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Decorative window arch background
                g2.setColor(new Color(255, 225, 200, 100));
                int archW = Math.min(260, w - 80);
                int archH = Math.min(320, h - 220);
                int archX = (w - archW) / 2;
                int archY = h - archH - 60;
                g2.fillRoundRect(archX, archY, archW, archH, archW, archW);

                // Draw book stack illustration
                drawBookStack(g2, w / 2, h - 90);

                g2.dispose();
            }
        };
        left.setLayout(new BorderLayout());
        left.setBorder(new EmptyBorder(50, 40, 40, 40));

        // Top Branding
        JPanel brandTop = new JPanel();
        brandTop.setLayout(new BoxLayout(brandTop, BoxLayout.Y_AXIS));
        brandTop.setOpaque(false);

        // Orange open book icon
        JLabel iconLogo = new JLabel(UIUtils.createIcon("book", 44, ThemeColors.PRIMARY));
        iconLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Library Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(ThemeColors.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle2 = new JLabel("System");
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle2.setForeground(ThemeColors.PRIMARY);
        lblTitle2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTagline = new JLabel("Manage Books | Track Borrowers | Build a Smarter Library");
        lblTagline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTagline.setForeground(ThemeColors.TEXT_MUTED);
        lblTagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandTop.add(iconLogo);
        brandTop.add(Box.createVerticalStrut(14));
        brandTop.add(lblTitle);
        brandTop.add(lblTitle2);
        brandTop.add(Box.createVerticalStrut(8));
        brandTop.add(lblTagline);

        left.add(brandTop, BorderLayout.NORTH);
        return left;
    }

    private void drawBookStack(Graphics2D g2, int centerX, int baseY) {
        // Draw stylized book spines
        int[][] books = {
                {190, 24, 0xFF6B00, 0xE05500}, // Bottom book (orange)
                {170, 22, 0x1E293B, 0x0F172A}, // Second book (dark charcoal)
                {155, 20, 0xFF9E54, 0xEA7A24}, // Third book (peach)
                {140, 18, 0xFF6B00, 0xC44E00}, // Top book (orange)
        };

        int currY = baseY;
        for (int[] b : books) {
            int bw = b[0];
            int bh = b[1];
            Color cMain = new Color(b[2]);
            Color cBorder = new Color(b[3]);

            currY -= bh + 3;
            g2.setColor(cMain);
            g2.fillRoundRect(centerX - bw / 2, currY, bw, bh, 6, 6);
            g2.setColor(cBorder);
            g2.drawRoundRect(centerX - bw / 2, currY, bw, bh, 6, 6);

            // Book spine stripe
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRect(centerX - bw / 2 + 12, currY + 4, 3, bh - 8);
        }
    }

    private JPanel createRightLoginPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setOpaque(false);
        formCard.setPreferredSize(new Dimension(360, 480));

        // Welcome Back
        JLabel lblWelcome = new JLabel("Welcome Back");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Login to continue to your account");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        formCard.add(lblWelcome);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(lblSub);
        formCard.add(Box.createVerticalStrut(24));

        // Error message label
        lblError = new JLabel(" ");
        lblError.setFont(ThemeColors.FONT_SUBTEXT);
        lblError.setForeground(ThemeColors.BADGE_DANGER_FG);
        formCard.add(lblError);
        formCard.add(Box.createVerticalStrut(6));

        // Username Field with Icon
        txtUsername = new RoundedTextField("Username", 20);
        txtUsername.setLeadingIcon(UIUtils.createIcon("user", 16, ThemeColors.TEXT_LIGHT));
        txtUsername.setText("admin"); // Default convenience
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        formCard.add(txtUsername);
        formCard.add(Box.createVerticalStrut(14));

        // Password Field with Icon
        txtPassword = new RoundedPasswordField("Password", 20);
        txtPassword.setLeadingIcon(UIUtils.createIcon("lock", 16, ThemeColors.TEXT_LIGHT));
        txtPassword.setText("admin123"); // Default convenience
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        formCard.add(txtPassword);
        formCard.add(Box.createVerticalStrut(12));

        // Remember Me & Forgot Password Row
        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);
        optionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        chkRemember = new JCheckBox("Remember me");
        chkRemember.setFont(ThemeColors.FONT_SUBTEXT);
        chkRemember.setForeground(ThemeColors.TEXT_MUTED);
        chkRemember.setOpaque(false);
        chkRemember.setSelected(true);
        optionsRow.add(chkRemember, BorderLayout.WEST);

        JLabel lblForgot = new JLabel("Forgot password?");
        lblForgot.setFont(ThemeColors.FONT_SUBTEXT);
        lblForgot.setForeground(ThemeColors.PRIMARY);
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        optionsRow.add(lblForgot, BorderLayout.EAST);
        lblForgot.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Please contact your library system administrator to reset your password.",
                        "Password assistance", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        formCard.add(optionsRow);
        formCard.add(Box.createVerticalStrut(22));

        // Login Button
        RoundedButton btnLogin = new RoundedButton("Login");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setPreferredSize(new Dimension(360, 44));
        btnLogin.addActionListener(e -> performLogin());
        formCard.add(btnLogin);
        formCard.add(Box.createVerticalStrut(16));

        // "or" Divider
        JPanel dividerPanel = new JPanel(new GridBagLayout());
        dividerPanel.setOpaque(false);
        dividerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JSeparator s1 = new JSeparator();
        s1.setForeground(ThemeColors.BORDER);
        JSeparator s2 = new JSeparator();
        s2.setForeground(ThemeColors.BORDER);
        JLabel lblOr = new JLabel("  or  ");
        lblOr.setFont(ThemeColors.FONT_SUBTEXT);
        lblOr.setForeground(ThemeColors.TEXT_LIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        dividerPanel.add(s1, gbc);
        gbc.weightx = 0;
        dividerPanel.add(lblOr, gbc);
        gbc.weightx = 1.0;
        dividerPanel.add(s2, gbc);
        formCard.add(dividerPanel);
        formCard.add(Box.createVerticalStrut(16));

        // Sign in as Admin Outline Button
        RoundedButton btnQuickAdmin = new RoundedButton("Sign in as Admin", true);
        btnQuickAdmin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnQuickAdmin.setPreferredSize(new Dimension(360, 44));
        btnQuickAdmin.setIcon(UIUtils.createIcon("lock", 16, ThemeColors.PRIMARY));
        btnQuickAdmin.addActionListener(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
            performLogin();
        });
        formCard.add(btnQuickAdmin);
        formCard.add(Box.createVerticalStrut(24));

        // Footer Note
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        JLabel lblContact = new JLabel("Don't have an account? ");
        lblContact.setFont(ThemeColors.FONT_SUBTEXT);
        lblContact.setForeground(ThemeColors.TEXT_MUTED);

        JLabel lblAdminLink = new JLabel("Contact Admin");
        lblAdminLink.setFont(ThemeColors.FONT_BODY_BOLD);
        lblAdminLink.setForeground(ThemeColors.PRIMARY);
        lblAdminLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        footer.add(lblContact);
        footer.add(lblAdminLink);
        lblAdminLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Ask the system administrator to create or enable an account.",
                        "Contact administrator", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        formCard.add(footer);

        right.add(formCard);
        return right;
    }

    private void performLogin() {
        lblError.setText(" ");
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            lblError.setText("Please enter your username.");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            lblError.setText("Please enter your password.");
            txtPassword.requestFocus();
            return;
        }

        try {
            // Create the starter account only after the database has been initialized.
            // Doing this synchronously avoids the old race where login ran before
            // the background admin-creation thread finished.
            new AdminDAO().ensureDefaultAdminExists();
            Admin admin = authService.authenticate(username, password);
            if (admin != null) {
                // Login Success!
                DashboardFrame dashboard = new DashboardFrame(admin);
                dashboard.setVisible(true);
                dispose();
            } else {
                lblError.setText("Invalid username or password.");
            }
        } catch (Exception ex) {
            lblError.setText("Database error: " + ex.getMessage());
        }
    }
}
