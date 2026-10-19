package com.library.ui.panels;

import com.library.service.ReportService;
import com.library.ui.DashboardFrame;
import com.library.ui.components.ModernTable;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedPanel;
import com.library.ui.theme.ThemeColors;
import com.library.util.CsvExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReportsPanel extends JPanel {

    private final ReportService reportService;

    private JComboBox<String> cmbReportType;
    private ModernTable table;
    private DefaultTableModel tableModel;

    public ReportsPanel(DashboardFrame parentFrame) {
        this.reportService = new ReportService();

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadSelectedReport();
    }

    private void initUI() {
        // Top Container
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Reports & Analytics");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Generate, inspect, and export library performance metrics to CSV");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(lblSub);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Export to CSV Button
        RoundedButton btnExport = new RoundedButton("Export to CSV");
        btnExport.setPreferredSize(new Dimension(140, 38));
        btnExport.addActionListener(e -> exportCurrentReport());
        headerPanel.add(btnExport, BorderLayout.EAST);

        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(18));

        // Selector Bar
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectorBar.setOpaque(false);

        JLabel lblSelect = new JLabel("Select Report:");
        lblSelect.setFont(ThemeColors.FONT_BODY_BOLD);
        lblSelect.setForeground(ThemeColors.TEXT_DARK);
        selectorBar.add(lblSelect);

        String[] reports = {
                "Book Inventory Report",
                "Currently Issued Books",
                "Overdue Borrowers Report",
                "Most Popular Books",
                "Fine Collection History"
        };
        cmbReportType = new JComboBox<>(reports);
        cmbReportType.setFont(ThemeColors.FONT_BODY);
        cmbReportType.setBackground(ThemeColors.BG_CARD);
        cmbReportType.setPreferredSize(new Dimension(240, 34));
        cmbReportType.addActionListener(e -> loadSelectedReport());
        selectorBar.add(cmbReportType);

        topContainer.add(selectorBar);
        topContainer.add(Box.createVerticalStrut(14));
        add(topContainer, BorderLayout.NORTH);

        // Center Table in Card
        RoundedPanel tableCard = new RoundedPanel(14, ThemeColors.BG_CARD, ThemeColors.BORDER);
        tableCard.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        table = new ModernTable(tableModel);

        tableCard.add(ModernTable.createModernScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private void loadSelectedReport() {
        String selected = (String) cmbReportType.getSelectedItem();
        if (selected == null) return;

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                switch (selected) {
                    case "Currently Issued Books":
                        return reportService.getCurrentlyIssuedReport();
                    case "Overdue Borrowers Report":
                        return reportService.getOverdueBooksReport();
                    case "Most Popular Books":
                        return reportService.getMostPopularBooksReport();
                    case "Fine Collection History":
                        return reportService.getFineCollectionReport();
                    case "Book Inventory Report":
                    default:
                        return reportService.getBookInventoryReport();
                }
            }

            @Override
            protected void done() {
                try {
                    tableModel = get();
                    table.setModel(tableModel);
                } catch (Exception e) {
                    System.err.println("Could not load report: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void exportCurrentReport() {
        String selected = (String) cmbReportType.getSelectedItem();
        String safeName = selected != null ? selected.replaceAll("\\s+", "_").toLowerCase() : "library_report";
        CsvExporter.exportTableToCsv(table, this, safeName);
    }
}
