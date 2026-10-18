package com.library.ui.panels;

import com.library.model.ActivityLog;
import com.library.model.DashboardStats;
import com.library.service.LibraryService;
import com.library.ui.DashboardFrame;
import com.library.ui.components.ModernChartPanel;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedPanel;
import com.library.ui.components.StatCard;
import com.library.ui.theme.ThemeColors;
import com.library.ui.theme.UIUtils;
import com.library.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final LibraryService libraryService;

    private StatCard cardTotalBooks;
    private StatCard cardActiveMembers;
    private StatCard cardBooksIssued;
    private StatCard cardPendingReturns;

    private ModernChartPanel chartPanel;
    private JPanel activitiesListContainer;

    public DashboardPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.libraryService = new LibraryService();

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Main scrollable content or vertical box
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // 1. Header Section
        contentPanel.add(createHeaderPanel());
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Stat Cards Grid (4 cards)
        contentPanel.add(createStatCardsGrid());
        contentPanel.add(Box.createVerticalStrut(20));

        // 3. Middle Section: Chart on left (60%), Recent Activities on right (40%)
        contentPanel.add(createMiddleSection());
        contentPanel.add(Box.createVerticalStrut(20));

        // 4. Quick Actions bar
        contentPanel.add(createQuickActionsBar());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new com.library.ui.components.CustomScrollBarUI());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        // Title and subtext
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Overview of your library system");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(lblSub);

        p.add(titlePanel, BorderLayout.WEST);

        // Right side: Date Pill (Oct 18, 2026 Sunday)
        RoundedPanel datePill = new RoundedPanel(16, Color.WHITE, ThemeColors.BORDER);
        datePill.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 6));

        String dateStr = DateUtil.formatHeaderDate(LocalDate.now());
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(ThemeColors.FONT_SUBTEXT);
        lblDate.setForeground(ThemeColors.TEXT_MUTED);
        datePill.add(lblDate);

        p.add(datePill, BorderLayout.EAST);
        return p;
    }

    private JPanel createStatCardsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);

        cardTotalBooks = new StatCard("Total Books", "1,248", "book");
        cardActiveMembers = new StatCard("Active Members", "320", "users");
        cardBooksIssued = new StatCard("Books Issued", "186", "clock");
        cardPendingReturns = new StatCard("Pending Returns", "24", "alert");

        grid.add(cardTotalBooks);
        grid.add(cardActiveMembers);
        grid.add(cardBooksIssued);
        grid.add(cardPendingReturns);

        return grid;
    }

    private JPanel createMiddleSection() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.weighty = 1.0;

        // Left: Bar chart (approx 60% width)
        gbc.gridx = 0;
        gbc.weightx = 0.60;
        chartPanel = new ModernChartPanel();
        chartPanel.setPreferredSize(new Dimension(520, 320));
        container.add(chartPanel, gbc);

        // Right: Recent Activities (approx 40% width)
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 0, 0);
        gbc.weightx = 0.40;
        RoundedPanel activitiesCard = createRecentActivitiesCard();
        activitiesCard.setPreferredSize(new Dimension(380, 320));
        container.add(activitiesCard, gbc);

        return container;
    }

    private RoundedPanel createRecentActivitiesCard() {
        RoundedPanel card = new RoundedPanel(14, ThemeColors.BG_CARD, ThemeColors.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Recent Activities");
        title.setFont(ThemeColors.FONT_CARD_TITLE);
        title.setForeground(ThemeColors.TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        JButton viewAll = new JButton("View All");
        viewAll.setFont(ThemeColors.FONT_SUBTEXT);
        viewAll.setForeground(ThemeColors.PRIMARY);
        viewAll.setContentAreaFilled(false);
        viewAll.setBorderPainted(false);
        viewAll.setFocusPainted(false);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(e -> parentFrame.navigateTo("reports"));
        header.add(viewAll, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // Activity Items List
        activitiesListContainer = new JPanel();
        activitiesListContainer.setLayout(new BoxLayout(activitiesListContainer, BoxLayout.Y_AXIS));
        activitiesListContainer.setOpaque(false);
        activitiesListContainer.setBorder(new EmptyBorder(10, 0, 0, 0));

        card.add(activitiesListContainer, BorderLayout.CENTER);
        return card;
    }

    private JPanel createQuickActionsBar() {
        RoundedPanel bar = new RoundedPanel(14, Color.WHITE, ThemeColors.BORDER);
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 12));
        bar.setBorder(new EmptyBorder(2, 8, 2, 8));

        JLabel lblQuick = new JLabel("Quick Actions:");
        lblQuick.setFont(ThemeColors.FONT_BODY_BOLD);
        lblQuick.setForeground(ThemeColors.TEXT_DARK);
        bar.add(lblQuick);

        RoundedButton btnAddBook = new RoundedButton("+ Add Book");
        btnAddBook.setPreferredSize(new Dimension(130, 36));
        btnAddBook.addActionListener(e -> {
            parentFrame.navigateTo("books");
            BooksPanel bp = (BooksPanel) parentFrame.getPanel("books");
            if (bp != null) bp.openAddBookDialog();
        });
        bar.add(btnAddBook);

        RoundedButton btnAddMember = new RoundedButton("+ Register Member", true);
        btnAddMember.setPreferredSize(new Dimension(150, 36));
        btnAddMember.addActionListener(e -> {
            parentFrame.navigateTo("members");
            MembersPanel mp = (MembersPanel) parentFrame.getPanel("members");
            if (mp != null) mp.openAddMemberDialog();
        });
        bar.add(btnAddMember);

        RoundedButton btnIssue = new RoundedButton("Issue Book", true);
        btnIssue.setPreferredSize(new Dimension(130, 36));
        btnIssue.addActionListener(e -> parentFrame.navigateTo("issuereturn"));
        bar.add(btnIssue);

        return bar;
    }

    public void refreshData() {
        // Run in background worker to keep UI fluid
        new SwingWorker<Void, Void>() {
            DashboardStats stats;
            List<ActivityLog> activities;
            java.util.Map<String, int[]> weeklyData;

            @Override
            protected Void doInBackground() throws Exception {
                stats = libraryService.getDashboardStats();
                activities = libraryService.getRecentActivities(4);
                weeklyData = libraryService.getWeeklyChartData();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "Dashboard loading was interrupted.", "Dashboard Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "Could not load dashboard data: " + cause.getMessage(),
                            "Dashboard Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                NumberFormat nf = NumberFormat.getInstance(Locale.US);

                if (stats != null) {
                    cardTotalBooks.setValue(nf.format(stats.getTotalBooks()));
                    cardActiveMembers.setValue(nf.format(stats.getActiveMembers()));
                    cardBooksIssued.setValue(nf.format(stats.getBooksIssued()));
                    cardPendingReturns.setValue(nf.format(stats.getPendingReturns()));
                }

                if (weeklyData != null && chartPanel != null) {
                    chartPanel.setChartData(weeklyData);
                }

                if (activities != null && activitiesListContainer != null) {
                    activitiesListContainer.removeAll();
                    for (ActivityLog act : activities) {
                        activitiesListContainer.add(createActivityItem(act));
                        activitiesListContainer.add(Box.createVerticalStrut(10));
                    }
                    activitiesListContainer.revalidate();
                    activitiesListContainer.repaint();
                }
            }
        }.execute();
    }

    private JPanel createActivityItem(ActivityLog act) {
        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setOpaque(false);

        // Circular Icon
        String iconType = "book";
        if (act.getActionType().contains("RETURN")) iconType = "clock";
        else if (act.getActionType().contains("MEMBER")) iconType = "users";
        else if (act.getActionType().contains("FINE")) iconType = "fines";

        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColors.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(36, 36));
        iconCircle.setLayout(new GridBagLayout());
        iconCircle.add(new JLabel(UIUtils.createIcon(iconType, 16, ThemeColors.PRIMARY)));

        item.add(iconCircle, BorderLayout.WEST);

        // Description text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel desc = new JLabel(act.getDescription());
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(ThemeColors.TEXT_DARK);

        JLabel time = new JLabel(DateUtil.getRelativeTime(act.getCreatedAt()));
        time.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        time.setForeground(ThemeColors.TEXT_LIGHT);

        textPanel.add(desc);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(time);

        item.add(textPanel, BorderLayout.CENTER);
        return item;
    }
}
