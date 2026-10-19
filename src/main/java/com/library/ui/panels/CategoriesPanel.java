package com.library.ui.panels;

import com.library.model.Category;
import com.library.service.CategoryService;
import com.library.ui.DashboardFrame;
import com.library.ui.components.ModernTable;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedPanel;
import com.library.ui.components.RoundedTextField;
import com.library.ui.theme.ThemeColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriesPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final CategoryService categoryService;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private List<Category> categoriesList = new ArrayList<>();

    public CategoriesPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.categoryService = new CategoryService();

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadCategoriesData();
    }

    private void initUI() {
        // Top Container: Header
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Categories");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Organize and manage book categories");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(lblSub);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        RoundedButton btnAdd = new RoundedButton("+ Add Category");
        btnAdd.setPreferredSize(new Dimension(140, 38));
        btnAdd.addActionListener(e -> openAddCategoryDialog(null));
        headerPanel.add(btnAdd, BorderLayout.EAST);

        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(20));
        add(topContainer, BorderLayout.NORTH);

        // Center Table in Card
        RoundedPanel tableCard = new RoundedPanel(14, ThemeColors.BG_CARD, ThemeColors.BORDER);
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"#", "Category Name", "Description", "Books in Category", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        table = new ModernTable(tableModel);
        table.setActionColumn(4, new ModernTable.ActionCallback() {
            @Override
            public void onEdit(int row) {
                if (row >= 0 && row < categoriesList.size()) {
                    openAddCategoryDialog(categoriesList.get(row));
                }
            }

            @Override
            public void onDelete(int row) {
                deleteCategoryAtRow(row);
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        tableCard.add(ModernTable.createModernScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    public void loadCategoriesData() {
        new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() throws Exception {
                return categoryService.getAllCategories();
            }

            @Override
            protected void done() {
                try {
                    categoriesList = get();
                    tableModel.setRowCount(0);
                    for (int i = 0; i < categoriesList.size(); i++) {
                        Category c = categoriesList.get(i);
                        tableModel.addRow(new Object[]{
                                (i + 1),
                                c.getCategoryName(),
                                c.getDescription() != null ? c.getDescription() : "-",
                                c.getBookCount() + " books",
                                ""
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Could not load categories: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void openAddCategoryDialog(Category toEdit) {
        JDialog dialog = new JDialog(parentFrame, toEdit == null ? "Add Category" : "Edit Category", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setResizable(false);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(ThemeColors.BG_CARD);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JLabel lblTitle = new JLabel(toEdit == null ? "New Category" : "Edit Category");
        lblTitle.setFont(ThemeColors.FONT_SECTION_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);
        form.add(lblTitle);
        form.add(Box.createVerticalStrut(14));

        RoundedTextField txtName = new RoundedTextField("Enter category name", 20);
        if (toEdit != null) txtName.setText(toEdit.getCategoryName());
        form.add(createFieldWrapper("Category Name *", txtName));
        form.add(Box.createVerticalStrut(10));

        RoundedTextField txtDesc = new RoundedTextField("Description (optional)", 20);
        if (toEdit != null) txtDesc.setText(toEdit.getDescription() != null ? toEdit.getDescription() : "");
        form.add(createFieldWrapper("Description", txtDesc));
        form.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Cancel", true);
        btnCancel.setPreferredSize(new Dimension(80, 34));
        btnCancel.addActionListener(e -> dialog.dispose());
        btnRow.add(btnCancel);

        RoundedButton btnSave = new RoundedButton(toEdit == null ? "Create" : "Save");
        btnSave.setPreferredSize(new Dimension(90, 34));
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String desc = txtDesc.getText().trim();
                Category cat = (toEdit != null) ? toEdit : new Category();
                cat.setCategoryName(name);
                cat.setDescription(desc);

                categoryService.saveCategory(cat);
                dialog.dispose();
                loadCategoriesData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnRow.add(btnSave);

        p.add(form, BorderLayout.CENTER);
        p.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(p);
        dialog.setVisible(true);
    }

    private void deleteCategoryAtRow(int row) {
        if (row >= 0 && row < categoriesList.size()) {
            Category c = categoriesList.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete category '" + c.getCategoryName() + "'?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    categoryService.deleteCategory(c.getCategoryId());
                    JOptionPane.showMessageDialog(this, "Category deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCategoriesData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
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
}
