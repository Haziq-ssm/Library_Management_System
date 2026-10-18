package com.library.ui.components;

import com.library.ui.theme.ThemeColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModernChartPanel extends RoundedPanel {

    private Map<String, int[]> chartData = new LinkedHashMap<>();
    private final int MAX_VALUE = 40;

    public ModernChartPanel() {
        super(14, ThemeColors.BG_CARD, ThemeColors.BORDER);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Header with Title and Legend
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Book Issue/Return (Last 7 Days)");
        titleLabel.setFont(ThemeColors.FONT_CARD_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Legend Panel on East
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        legendPanel.setOpaque(false);

        legendPanel.add(createLegendItem("Issued", ThemeColors.CHART_ISSUED));
        legendPanel.add(createLegendItem("Returned", ThemeColors.CHART_RETURNED));
        headerPanel.add(legendPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Canvas for custom bar chart drawing
        JPanel chartCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                drawChart(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        chartCanvas.setOpaque(false);
        chartCanvas.setBorder(new EmptyBorder(15, 10, 10, 10));
        add(chartCanvas, BorderLayout.CENTER);

        // Default initial data
        loadDefaultData();
    }

    private JPanel createLegendItem(String labelText, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);

        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 2, 10, 10, 3, 3);
                g2.dispose();
            }
        };
        indicator.setOpaque(false);
        indicator.setPreferredSize(new Dimension(10, 14));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeColors.FONT_SUBTEXT);
        lbl.setForeground(ThemeColors.TEXT_MUTED);

        p.add(indicator);
        p.add(lbl);
        return p;
    }

    public void setChartData(Map<String, int[]> data) {
        if (data != null && !data.isEmpty()) {
            this.chartData = data;
            repaint();
        }
    }

    private void loadDefaultData() {
        chartData.put("Mon", new int[]{0, 0});
        chartData.put("Tue", new int[]{0, 0});
        chartData.put("Wed", new int[]{0, 0});
        chartData.put("Thu", new int[]{0, 0});
        chartData.put("Fri", new int[]{0, 0});
        chartData.put("Sat", new int[]{0, 0});
        chartData.put("Sun", new int[]{0, 0});
    }

    private void drawChart(Graphics2D g2, int width, int height) {
        int leftMargin = 35;
        int rightMargin = 20;
        int topMargin = 15;
        int bottomMargin = 30;

        int chartWidth = width - leftMargin - rightMargin;
        int chartHeight = height - topMargin - bottomMargin;

        if (chartWidth <= 0 || chartHeight <= 0) return;

        // Draw Y-Axis Grid & Labels (0, 10, 20, 30, 40)
        int steps = 4;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i <= steps; i++) {
            int val = (MAX_VALUE / steps) * i;
            int y = topMargin + chartHeight - (int) ((double) val / MAX_VALUE * chartHeight);

            // Grid line
            g2.setColor(ThemeColors.CHART_GRID);
            g2.drawLine(leftMargin, y, leftMargin + chartWidth, y);

            // Label
            g2.setColor(ThemeColors.TEXT_LIGHT);
            String valStr = String.valueOf(val);
            g2.drawString(valStr, leftMargin - fm.stringWidth(valStr) - 8, y + 4);
        }

        // Draw Bars and X-Axis Labels
        int numGroups = chartData.size();
        if (numGroups == 0) return;

        double groupSlot = (double) chartWidth / numGroups;
        int barWidth = Math.max(8, (int) (groupSlot * 0.22));
        int barSpacing = 4;

        int index = 0;
        for (Map.Entry<String, int[]> entry : chartData.entrySet()) {
            String day = entry.getKey();
            int[] values = entry.getValue();

            int issued = values.length > 0 ? values[0] : 0;
            int returned = values.length > 1 ? values[1] : 0;

            double groupCenterX = leftMargin + (index * groupSlot) + (groupSlot / 2.0);

            // Bar 1: Issued
            int bar1Height = (int) (Math.min(issued, MAX_VALUE) / (double) MAX_VALUE * chartHeight);
            int bar1X = (int) (groupCenterX - barWidth - (barSpacing / 2.0));
            int bar1Y = topMargin + chartHeight - bar1Height;

            g2.setColor(ThemeColors.CHART_ISSUED);
            g2.fillRoundRect(bar1X, bar1Y, barWidth, bar1Height, 4, 4);

            // Bar 2: Returned
            int bar2Height = (int) (Math.min(returned, MAX_VALUE) / (double) MAX_VALUE * chartHeight);
            int bar2X = (int) (groupCenterX + (barSpacing / 2.0));
            int bar2Y = topMargin + chartHeight - bar2Height;

            g2.setColor(ThemeColors.CHART_RETURNED);
            g2.fillRoundRect(bar2X, bar2Y, barWidth, bar2Height, 4, 4);

            // Day label below axis
            g2.setColor(ThemeColors.TEXT_MUTED);
            int labelWidth = fm.stringWidth(day);
            g2.drawString(day, (int) (groupCenterX - (labelWidth / 2.0)), topMargin + chartHeight + 18);

            index++;
        }
    }
}
