package com.library.ui.panels;

import com.library.model.FinePayment;
import com.library.model.LibraryTransaction;
import com.library.service.FineService;
import com.library.ui.DashboardFrame;
import com.library.ui.components.ModernTable;
import com.library.ui.components.RoundedPanel;
import com.library.ui.components.StatCard;
import com.library.ui.theme.ThemeColors;
import com.library.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FinesPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final FineService fineService;

    private StatCard cardCollected;
    private StatCard cardOutstanding;

    private JButton btnTabPending;
    private JButton btnTabHistory;

    private ModernTable table;
    private DefaultTableModel pendingModel;
    private DefaultTableModel historyModel;

    private List<LibraryTransaction> pendingFinesList = new ArrayList<>();
    private List<FinePayment> paymentHistoryList = new ArrayList<>();

    public FinesPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.fineService = new FineService();

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadData();
    }

    private void initUI() {
        // Top Container
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header Title
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Fines & Payments");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Track overdue fines, settlements, and payment history");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(lblSub);
        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(18));

        // Stat Cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        statsRow.setOpaque(false);

        cardCollected = new StatCard("Total Fines Collected", "₹0.00", "fines");
        cardOutstanding = new StatCard("Outstanding Balance", "₹0.00", "alert");

        statsRow.add(cardCollected);
        statsRow.add(cardOutstanding);
        topContainer.add(statsRow);
        topContainer.add(Box.createVerticalStrut(18));

        // Tab Switchers
        JPanel tabsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tabsRow.setOpaque(false);

        btnTabPending = createTabButton("Pending & Overdue Fines", true);
        btnTabPending.addActionListener(e -> switchTab(true));

        btnTabHistory = createTabButton("Payment History", false);
        btnTabHistory.addActionListener(e -> switchTab(false));

        tabsRow.add(btnTabPending);
        tabsRow.add(btnTabHistory);

        topContainer.add(tabsRow);
        topContainer.add(Box.createVerticalStrut(14));
        add(topContainer, BorderLayout.NORTH);

        // Center Table in Card
        RoundedPanel tableCard = new RoundedPanel(14, ThemeColors.BG_CARD, ThemeColors.BORDER);
        tableCard.setLayout(new BorderLayout());

        // Models
        String[] pendingCols = {"#", "Trans ID", "Member", "Book Title", "Due Date", "Days Overdue", "Total Fine", "Paid", "Balance", "Action"};
        pendingModel = new DefaultTableModel(pendingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9;
            }
        };

        String[] historyCols = {"Receipt ID", "Member Name", "Member Code", "Book Title", "Amount (₹)", "Payment Date", "Method", "Notes"};
        historyModel = new DefaultTableModel(historyCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(pendingModel);
        installPendingActionColumn();

        tableCard.add(ModernTable.createModernScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeColors.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateTabButtonStyle(btn, active);
        return btn;
    }

    private void updateTabButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(ThemeColors.PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
        } else {
            btn.setBackground(ThemeColors.BG_INPUT);
            btn.setForeground(ThemeColors.TEXT_MUTED);
            btn.setOpaque(true);
        }
    }

    private void switchTab(boolean showPending) {
        updateTabButtonStyle(btnTabPending, showPending);
        updateTabButtonStyle(btnTabHistory, !showPending);

        if (showPending) {
            table.setModel(pendingModel);
            installPendingActionColumn();
        } else {
            table.setModel(historyModel);
        }
        loadData();
    }

    private void installPendingActionColumn() {
        table.getColumnModel().getColumn(9).setCellRenderer((t, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("Pay Fine");
            btn.setFont(ThemeColors.FONT_BADGE);
            btn.setBackground(ThemeColors.PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        });

        table.getColumnModel().getColumn(9).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private final JButton btn = new JButton("Pay Fine");
            private int currentRow;

            {
                btn.setFont(ThemeColors.FONT_BADGE);
                btn.setBackground(ThemeColors.PRIMARY);
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.addActionListener(e -> {
                    fireEditingStopped();
                    openPayDialog(currentRow);
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.currentRow = row;
                return btn;
            }

            @Override
            public Object getCellEditorValue() {
                return "Pay Fine";
            }
        });
        table.getColumnModel().getColumn(9).setPreferredWidth(90);
    }

    public void loadData() {
        new SwingWorker<Void, Void>() {
            double totalCollected = 0.0;
            double totalOutstanding = 0.0;

            @Override
            protected Void doInBackground() throws Exception {
                totalCollected = fineService.getTotalCollected();
                totalOutstanding = fineService.getTotalOutstanding();
                pendingFinesList = fineService.getTransactionsWithFines();
                paymentHistoryList = fineService.getAllPaymentHistory();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(FinesPanel.this,
                            "Fine data loading was interrupted.", "Loading Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(FinesPanel.this,
                            "Could not load fine data: " + cause.getMessage(),
                            "Loading Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                cardCollected.setValue(String.format("₹%.2f", totalCollected));
                cardOutstanding.setValue(String.format("₹%.2f", totalOutstanding));

                // Populate Pending Model
                pendingModel.setRowCount(0);
                for (int i = 0; i < pendingFinesList.size(); i++) {
                    LibraryTransaction t = pendingFinesList.get(i);
                    pendingModel.addRow(new Object[]{
                            (i + 1),
                            "#" + t.getTransId(),
                            t.getMemberName() + " (" + t.getMemberCode() + ")",
                            t.getBookTitle(),
                            DateUtil.formatDisplay(t.getDueDate()),
                            t.getOverdueDays() + " days",
                            String.format("₹%.2f", t.getCalculatedFine()),
                            String.format("₹%.2f", t.getFinePaid()),
                            String.format("₹%.2f", t.getFineBalance()),
                            "Pay Fine"
                    });
                }

                // Populate History Model
                historyModel.setRowCount(0);
                for (FinePayment fp : paymentHistoryList) {
                    historyModel.addRow(new Object[]{
                            "#" + fp.getPaymentId(),
                            fp.getMemberName(),
                            fp.getMemberCode(),
                            fp.getBookTitle(),
                            String.format("%.2f", fp.getAmount()),
                            DateUtil.formatDisplay(fp.getPaymentDate()),
                            fp.getPaymentMethod(),
                            fp.getNotes() != null ? fp.getNotes() : "-"
                    });
                }
            }
        }.execute();
    }

    private void openPayDialog(int row) {
        if (row >= 0 && row < pendingFinesList.size()) {
            LibraryTransaction t = pendingFinesList.get(row);
            PayFineDialog dialog = new PayFineDialog(parentFrame, t);
            dialog.setVisible(true);
            if (dialog.isPaymentSuccess()) {
                loadData();
                parentFrame.refreshDashboardStats();
            }
        }
    }
}
