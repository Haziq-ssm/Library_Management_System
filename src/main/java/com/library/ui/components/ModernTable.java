package com.library.ui.components;

import com.library.ui.theme.ThemeColors;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;

public class ModernTable extends JTable {

    public interface ActionCallback {
        void onEdit(int row);
        void onDelete(int row);
    }

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    private void initStyle() {
        setRowHeight(42);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionBackground(ThemeColors.PRIMARY_LIGHT);
        setSelectionForeground(ThemeColors.TEXT_DARK);
        setFont(ThemeColors.FONT_BODY);
        setForeground(ThemeColors.TEXT_DARK);
        setBackground(ThemeColors.BG_CARD);

        // Header styling
        JTableHeader header = getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(ThemeColors.FONT_BODY_BOLD);
                lbl.setForeground(ThemeColors.TEXT_MUTED);
                lbl.setBackground(ThemeColors.BG_INPUT);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
                        new EmptyBorder(8, 12, 8, 12)
                ));
                return lbl;
            }
        });

        // Default cell renderer with zebra rows and padding
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                lbl.setFont(ThemeColors.FONT_BODY);

                if (isSelected) {
                    lbl.setBackground(ThemeColors.PRIMARY_LIGHT);
                    lbl.setForeground(ThemeColors.TEXT_DARK);
                } else {
                    lbl.setBackground(row % 2 == 0 ? ThemeColors.BG_CARD : ThemeColors.BG_MAIN);
                    lbl.setForeground(ThemeColors.TEXT_DARK);
                }
                return lbl;
            }
        });
    }

    /**
     * Installs the badge renderer on a specific column index.
     */
    public void setBadgeColumn(int columnIndex) {
        getColumnModel().getColumn(columnIndex).setCellRenderer(new TableCellRenderer() {
            private final BadgeLabel badge = new BadgeLabel("");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
                container.setOpaque(true);
                container.setBackground(isSelected ? ThemeColors.PRIMARY_LIGHT : (row % 2 == 0 ? ThemeColors.BG_CARD : ThemeColors.BG_MAIN));

                String val = value != null ? value.toString() : "";
                badge.setBadgeType(val);
                container.add(badge);
                return container;
            }
        });
    }

    /**
     * Installs action buttons (Edit / Delete) on a specific column index.
     */
    public void setActionColumn(int columnIndex, ActionCallback callback) {
        getColumnModel().getColumn(columnIndex).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return createActionPanel(row, isSelected, null);
            }
        });

        getColumnModel().getColumn(columnIndex).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JPanel panel;
            private int currentRow;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.currentRow = table.convertRowIndexToModel(row);
                ActionCallback editorCallback = callback == null ? null : new ActionCallback() {
                    @Override
                    public void onEdit(int ignoredRow) {
                        fireEditingStopped();
                        callback.onEdit(currentRow);
                    }

                    @Override
                    public void onDelete(int ignoredRow) {
                        fireEditingStopped();
                        callback.onDelete(currentRow);
                    }
                };
                this.panel = createActionPanel(row, isSelected, editorCallback);
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return "";
            }
        });

        getColumnModel().getColumn(columnIndex).setPreferredWidth(90);
        getColumnModel().getColumn(columnIndex).setMaxWidth(110);
    }

    private JPanel createActionPanel(int row, boolean isSelected, ActionCallback callback) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        p.setOpaque(true);
        p.setBackground(isSelected ? ThemeColors.PRIMARY_LIGHT : (row % 2 == 0 ? ThemeColors.BG_CARD : ThemeColors.BG_MAIN));

        JButton btnEdit = new JButton(UIUtils.createIcon("edit", 16, ThemeColors.TEXT_MUTED));
        btnEdit.setToolTipText("Edit");
        btnEdit.setPreferredSize(new Dimension(28, 28));
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnDelete = new JButton(UIUtils.createIcon("trash", 16, ThemeColors.BADGE_DANGER_FG));
        btnDelete.setToolTipText("Delete");
        btnDelete.setPreferredSize(new Dimension(28, 28));
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (callback != null) {
            btnEdit.addActionListener(e -> callback.onEdit(row));
            btnDelete.addActionListener(e -> callback.onDelete(row));
        }

        p.add(btnEdit);
        p.add(btnDelete);
        return p;
    }

    public static JScrollPane createModernScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(ThemeColors.BG_CARD);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        return scrollPane;
    }
}
