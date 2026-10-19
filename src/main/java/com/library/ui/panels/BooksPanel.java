package com.library.ui.panels;

import com.library.model.Book;
import com.library.model.Category;
import com.library.service.BookService;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class BooksPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final BookService bookService;
    private final CategoryService categoryService;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private List<Book> currentBooksList = new ArrayList<>();

    private RoundedTextField searchField;
    private JComboBox<String> cmbCategory;
    private JComboBox<String> cmbSort;
    private List<Category> categories = new ArrayList<>();

    private String currentStatusFilter = "All Books";
    private JButton[] filterTabs;

    // Pagination
    private int currentPage = 1;
    private final int pageSize = 8;
    private int totalPages = 1;
    private JPanel paginationPanel;

    public BooksPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.bookService = new BookService();
        this.categoryService = new CategoryService();

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadCategories();
        loadBooksData();
    }

    private void initUI() {
        // Top Container: Header + Filters
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. Header with Add Book button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Books");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Manage all books in the library");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(lblSub);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        RoundedButton btnAdd = new RoundedButton("+ Add Book");
        btnAdd.setPreferredSize(new Dimension(130, 38));
        btnAdd.addActionListener(e -> openAddBookDialog());
        headerPanel.add(btnAdd, BorderLayout.EAST);

        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(18));

        // 2. Filter Pills + Search & Dropdowns
        JPanel filterRow = new JPanel(new BorderLayout(15, 0));
        filterRow.setOpaque(false);

        // Left Filter Tabs (Pills)
        JPanel pillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pillsPanel.setOpaque(false);

        String[] tabNames = {"All Books", "Available", "Issued", "Lost/Damaged"};
        filterTabs = new JButton[tabNames.length];
        for (int i = 0; i < tabNames.length; i++) {
            final String tabName = tabNames[i];
            filterTabs[i] = createFilterTab(tabName, i == 0);
            final int tabIdx = i;
            filterTabs[i].addActionListener(e -> {
                currentStatusFilter = tabName;
                for (int j = 0; j < filterTabs.length; j++) {
                    updateFilterTabStyle(filterTabs[j], j == tabIdx);
                }
                currentPage = 1;
                loadBooksData();
            });
            pillsPanel.add(filterTabs[i]);
        }
        filterRow.add(pillsPanel, BorderLayout.WEST);

        // Right Controls: Search, Category, Sort
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        searchField = new RoundedTextField("Search by title, author, ISBN...", 16);
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                currentPage = 1;
                loadBooksData();
            }
        });
        rightControls.add(searchField);

        cmbCategory = new JComboBox<>(new String[]{"All Categories"});
        cmbCategory.setFont(ThemeColors.FONT_SUBTEXT);
        cmbCategory.setBackground(ThemeColors.BG_CARD);
        cmbCategory.setPreferredSize(new Dimension(140, 34));
        cmbCategory.addActionListener(e -> {
            currentPage = 1;
            loadBooksData();
        });
        rightControls.add(cmbCategory);

        cmbSort = new JComboBox<>(new String[]{"Sort by", "Title (A-Z)", "Title (Z-A)", "Author", "Available Copies"});
        cmbSort.setFont(ThemeColors.FONT_SUBTEXT);
        cmbSort.setBackground(ThemeColors.BG_CARD);
        cmbSort.setPreferredSize(new Dimension(130, 34));
        cmbSort.addActionListener(e -> loadBooksData());
        rightControls.add(cmbSort);

        filterRow.add(rightControls, BorderLayout.EAST);
        topContainer.add(filterRow);
        topContainer.add(Box.createVerticalStrut(14));

        add(topContainer, BorderLayout.NORTH);

        // Center Table in Rounded Card
        RoundedPanel tableCard = new RoundedPanel(14, ThemeColors.BG_CARD, ThemeColors.BORDER);
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"#", "Book Title", "Author", "Category", "Status", "Available Copies", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is interactive
            }
        };

        table = new ModernTable(tableModel);
        table.setBadgeColumn(4); // Status column badge
        table.setActionColumn(6, new ModernTable.ActionCallback() {
            @Override
            public void onEdit(int row) {
                editBookAtRow(row);
            }

            @Override
            public void onDelete(int row) {
                deleteBookAtRow(row);
            }
        });

        // Set column widths
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        tableCard.add(ModernTable.createModernScrollPane(table), BorderLayout.CENTER);

        // South Pagination Panel
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        paginationPanel.setOpaque(false);
        tableCard.add(paginationPanel, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private JButton createFilterTab(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeColors.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateFilterTabStyle(btn, active);
        return btn;
    }

    private void updateFilterTabStyle(JButton btn, boolean active) {
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

    private void loadCategories() {
        try {
            categories = categoryService.getAllCategories();
            cmbCategory.removeAllItems();
            cmbCategory.addItem("All Categories");
            for (Category c : categories) {
                cmbCategory.addItem(c.getCategoryName());
            }
        } catch (Exception e) {
            System.err.println("Could not load categories for dropdown: " + e.getMessage());
        }
    }

    public void loadBooksData() {
        String keyword = searchField != null ? searchField.getText().trim() : null;
        Integer catId = null;
        if (cmbCategory != null && cmbCategory.getSelectedIndex() > 0) {
            catId = categories.get(cmbCategory.getSelectedIndex() - 1).getCategoryId();
        }
        String sortBy = cmbSort != null && cmbSort.getSelectedIndex() > 0 ? (String) cmbSort.getSelectedItem() : null;

        final Integer finalCatId = catId;
        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                return bookService.searchAndFilter(keyword, finalCatId, currentStatusFilter, sortBy);
            }

            @Override
            protected void done() {
                try {
                    currentBooksList = get();
                    updateTableData();
                } catch (Exception e) {
                    System.err.println("Failed to fetch books: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void updateTableData() {
        tableModel.setRowCount(0);
        int totalItems = currentBooksList.size();
        totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;

        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        for (int i = startIndex; i < endIndex; i++) {
            Book b = currentBooksList.get(i);
            tableModel.addRow(new Object[]{
                    (i + 1),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategoryName() != null ? b.getCategoryName() : "General",
                    b.getStatus(),
                    b.getAvailableCopies(),
                    ""
            });
        }

        renderPaginationControls();
    }

    private void renderPaginationControls() {
        paginationPanel.removeAll();

        JButton btnPrev = new JButton("Previous");
        btnPrev.setFont(ThemeColors.FONT_SUBTEXT);
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableData();
            }
        });
        paginationPanel.add(btnPrev);

        for (int p = 1; p <= totalPages; p++) {
            final int pageNum = p;
            JButton btnPage = new JButton(String.valueOf(p));
            btnPage.setFont(ThemeColors.FONT_SUBTEXT);
            if (p == currentPage) {
                btnPage.setBackground(ThemeColors.PRIMARY);
                btnPage.setForeground(Color.WHITE);
                btnPage.setOpaque(true);
            } else {
                btnPage.setBackground(ThemeColors.BG_CARD);
                btnPage.setForeground(ThemeColors.TEXT_DARK);
            }
            btnPage.addActionListener(e -> {
                currentPage = pageNum;
                updateTableData();
            });
            paginationPanel.add(btnPage);
        }

        JButton btnNext = new JButton("Next");
        btnNext.setFont(ThemeColors.FONT_SUBTEXT);
        btnNext.setEnabled(currentPage < totalPages);
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateTableData();
            }
        });
        paginationPanel.add(btnNext);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    public void openAddBookDialog() {
        AddEditBookDialog dialog = new AddEditBookDialog(parentFrame, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadBooksData();
            parentFrame.refreshDashboardStats();
        }
    }

    private void editBookAtRow(int row) {
        int actualIndex = (currentPage - 1) * pageSize + row;
        if (actualIndex >= 0 && actualIndex < currentBooksList.size()) {
            Book b = currentBooksList.get(actualIndex);
            AddEditBookDialog dialog = new AddEditBookDialog(parentFrame, b);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadBooksData();
                parentFrame.refreshDashboardStats();
            }
        }
    }

    private void deleteBookAtRow(int row) {
        int actualIndex = (currentPage - 1) * pageSize + row;
        if (actualIndex >= 0 && actualIndex < currentBooksList.size()) {
            Book b = currentBooksList.get(actualIndex);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete '" + b.getTitle() + "'?\nThis action cannot be undone.",
                    "Confirm Book Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    bookService.deleteBook(b.getBookId());
                    JOptionPane.showMessageDialog(this, "Book deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBooksData();
                    parentFrame.refreshDashboardStats();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete Book", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
