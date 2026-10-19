package com.library.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.library.model.Admin;
import com.library.service.AuthService;
import com.library.ui.components.RoundedPanel;
import com.library.ui.components.RoundedTextField;
import com.library.ui.panels.*;
import com.library.ui.theme.ThemeColors;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private final Admin loggedInAdmin;
    private JPanel mainContentCardPanel;
    private CardLayout cardLayout;
    private final Map<String, JPanel> registeredPanels = new HashMap<>();
    private final Map<String, JButton> sidebarButtons = new HashMap<>();
    private String currentActiveKey = "dashboard";

    public DashboardFrame(Admin admin) {
        super("Library Management System");
        this.loggedInAdmin = admin != null ? admin : new Admin("admin", "", "System Administrator", "admin@library.com");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeColors.BG_MAIN);

        // 1. Persistent Left Sidebar
        root.add(createSidebarPanel(), BorderLayout.WEST);

        // 2. Right Side: Top Bar + Card Content Area
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);

        rightContainer.add(createTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainContentCardPanel = new JPanel(cardLayout);
        mainContentCardPanel.setOpaque(false);

        // Register all screens
        DashboardPanel dashboardPanel = new DashboardPanel(this);
        BooksPanel booksPanel = new BooksPanel(this);
        MembersPanel membersPanel = new MembersPanel(this);
        IssueReturnPanel issueReturnPanel = new IssueReturnPanel(this);
        CategoriesPanel categoriesPanel = new CategoriesPanel(this);
        ReportsPanel reportsPanel = new ReportsPanel(this);
        FinesPanel finesPanel = new FinesPanel(this);
        SettingsPanel settingsPanel = new SettingsPanel(this);

        registeredPanels.put("dashboard", dashboardPanel);
        registeredPanels.put("books", booksPanel);
        registeredPanels.put("members", membersPanel);
        registeredPanels.put("issuereturn", issueReturnPanel);
        registeredPanels.put("categories", categoriesPanel);
        registeredPanels.put("reports", reportsPanel);
        registeredPanels.put("fines", finesPanel);
        registeredPanels.put("settings", settingsPanel);

        mainContentCardPanel.add(dashboardPanel, "dashboard");
        mainContentCardPanel.add(booksPanel, "books");
        mainContentCardPanel.add(membersPanel, "members");
        mainContentCardPanel.add(issueReturnPanel, "issuereturn");
        mainContentCardPanel.add(categoriesPanel, "categories");
        mainContentCardPanel.add(reportsPanel, "reports");
        mainContentCardPanel.add(finesPanel, "fines");
        mainContentCardPanel.add(settingsPanel, "settings");

        rightContainer.add(mainContentCardPanel, BorderLayout.CENTER);
        root.add(rightContainer, BorderLayout.CENTER);

        setContentPane(root);
        navigateTo(currentActiveKey);
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ThemeColors.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeColors.BORDER));

        // Top Brand Logo
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(6, 12, 10, 12));

        JLabel logoIcon = new JLabel(UIUtils.createIcon("book", 26, ThemeColors.PRIMARY));
        JLabel logoText = new JLabel("Library System");
        logoText.setFont(ThemeColors.FONT_SECTION_TITLE);
        logoText.setForeground(ThemeColors.TEXT_DARK);

        brandPanel.add(logoIcon);
        brandPanel.add(logoText);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Center Navigation Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        String[][] navItems = {
                {"dashboard", "Dashboard", "dashboard"},
                {"books", "Books", "book"},
                {"members", "Members", "users"},
                {"issuereturn", "Issue/Return", "issuereturn"},
                {"categories", "Categories", "categories"},
                {"reports", "Reports", "reports"},
                {"fines", "Fines", "fines"},
                {"settings", "Settings", "settings"}
        };

        for (String[] item : navItems) {
            String key = item[0];
            String label = item[1];
            String icon = item[2];

            JButton btn = createSidebarNavButton(key, label, icon);
            sidebarButtons.put(key, btn);
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(6));
        }

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Bottom Logout Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER));

        JButton btnLogout = new JButton("Logout", UIUtils.createIcon("logout", 18, ThemeColors.PRIMARY));
        btnLogout.setFont(ThemeColors.FONT_BODY_BOLD);
        btnLogout.setForeground(ThemeColors.PRIMARY);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> performLogout());

        bottomPanel.add(btnLogout);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createSidebarNavButton(String key, String text, String iconType) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = key.equals(currentActiveKey);
                if (isActive) {
                    g2.setColor(ThemeColors.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                } else if (getModel().isRollover()) {
                    g2.setColor(ThemeColors.PRIMARY_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(ThemeColors.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(14);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        updateSidebarButtonAppearance(btn, key, iconType);
        btn.addActionListener(e -> navigateTo(key));

        return btn;
    }

    private void updateSidebarButtonAppearance(JButton btn, String key, String iconType) {
        boolean isActive = key.equals(currentActiveKey);
        Color fg = isActive ? Color.WHITE : ThemeColors.TEXT_DARK;
        Color iconColor = isActive ? Color.WHITE : ThemeColors.TEXT_MUTED;

        btn.setForeground(fg);
        btn.setIcon(UIUtils.createIcon(iconType, 18, iconColor));
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeColors.BG_TOPBAR);
        topBar.setPreferredSize(new Dimension(0, 64));
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
            new EmptyBorder(10, 24, 10, 24)
        ));

        // Global Search Bar on Left
        RoundedTextField searchField = new RoundedTextField("Search books, members...", 24);
        searchField.setLeadingIcon(UIUtils.createIcon("search", 16, ThemeColors.TEXT_LIGHT));
        searchField.setPreferredSize(new Dimension(340, 38));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String query = searchField.getText().trim();
                    if (!query.isEmpty()) {
                        navigateTo("books");
                    }
                }
            }
        });
        topBar.add(searchField, BorderLayout.WEST);

        // Right side: Notifications + Admin profile pill
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 2));
        rightControls.setOpaque(false);

        // Notification Bell Button with badge dot
        JButton btnBell = new JButton(UIUtils.createIcon("bell", 20, ThemeColors.TEXT_MUTED)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Red badge dot
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColors.PRIMARY);
                g2.fillOval(getWidth() - 12, 4, 7, 7);
                g2.dispose();
            }
        };
        btnBell.setPreferredSize(new Dimension(38, 38));
        btnBell.setContentAreaFilled(false);
        btnBell.setBorderPainted(false);
        btnBell.setFocusPainted(false);
        btnBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBell.setToolTipText("Notifications");
        btnBell.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Notifications:\n- 3 books are due today.\n- 1 overdue fine was recorded.\n- System running smoothly.",
                "Library Notifications", JOptionPane.INFORMATION_MESSAGE));
        rightControls.add(btnBell);

        // Admin Profile Pill
        RoundedPanel profilePill = new RoundedPanel(18, ThemeColors.BG_INPUT, ThemeColors.BORDER);
        profilePill.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        profilePill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Avatar circle
        JPanel avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColors.PRIMARY);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        avatarCircle.setPreferredSize(new Dimension(26, 26));
        avatarCircle.setOpaque(false);
        avatarCircle.setLayout(new GridBagLayout());
        avatarCircle.add(new JLabel(UIUtils.createIcon("user", 14, Color.WHITE)));

        JLabel lblAdminName = new JLabel((loggedInAdmin.getFullName() != null ? loggedInAdmin.getFullName() : "Admin") + " ▾");
        lblAdminName.setFont(ThemeColors.FONT_BODY_BOLD);
        lblAdminName.setForeground(ThemeColors.TEXT_DARK);

        profilePill.add(avatarCircle);
        profilePill.add(lblAdminName);

        profilePill.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem itmSettings = new JMenuItem("Settings & Profile");
                itmSettings.addActionListener(ev -> navigateTo("settings"));

                JMenuItem itmLogout = new JMenuItem("Sign Out");
                itmLogout.addActionListener(ev -> performLogout());

                popup.add(itmSettings);
                popup.addSeparator();
                popup.add(itmLogout);
                popup.show(profilePill, 0, profilePill.getHeight() + 2);
            }
        });

        rightControls.add(profilePill);
        topBar.add(rightControls, BorderLayout.EAST);

        return topBar;
    }

    public void navigateTo(String key) {
        if (!registeredPanels.containsKey(key)) return;

        this.currentActiveKey = key;
        cardLayout.show(mainContentCardPanel, key);

        // Update sidebar button highlights
        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            String k = entry.getKey();
            JButton btn = entry.getValue();
            updateSidebarButtonAppearance(btn, k, k);
            btn.repaint();
        }

        // Trigger refresh if needed
        JPanel p = registeredPanels.get(key);
        if (p instanceof DashboardPanel) {
            ((DashboardPanel) p).refreshData();
        } else if (p instanceof BooksPanel) {
            ((BooksPanel) p).loadBooksData();
        } else if (p instanceof MembersPanel) {
            ((MembersPanel) p).loadMembersData();
        } else if (p instanceof IssueReturnPanel) {
            ((IssueReturnPanel) p).loadData();
        } else if (p instanceof CategoriesPanel) {
            ((CategoriesPanel) p).loadCategoriesData();
        } else if (p instanceof FinesPanel) {
            ((FinesPanel) p).loadData();
        }
    }

    public JPanel getPanel(String key) {
        return registeredPanels.get(key);
    }

    public void refreshDashboardStats() {
        JPanel p = registeredPanels.get("dashboard");
        if (p instanceof DashboardPanel) {
            ((DashboardPanel) p).refreshData();
        }
    }

    public void applyTheme(boolean darkMode) {
        String activeKey = currentActiveKey;
        ThemeColors.setDarkMode(darkMode);
        if (darkMode) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        UIManager.put("defaultFont", ThemeColors.FONT_BODY);
        registeredPanels.clear();
        sidebarButtons.clear();
        currentActiveKey = activeKey;
        initUI();
        revalidate();
        repaint();
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of the Library System?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
            dispose();
        }
    }
}
