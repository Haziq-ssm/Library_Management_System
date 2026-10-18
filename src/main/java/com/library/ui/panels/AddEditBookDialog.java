package com.library.ui.panels;

import com.library.model.Book;
import com.library.model.Category;
import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedPanel;
import com.library.ui.components.RoundedTextField;
import com.library.ui.theme.ThemeColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AddEditBookDialog extends JDialog {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final Book existingBook;
    private boolean saved = false;

    private RoundedTextField txtTitle;
    private RoundedTextField txtAuthor;
    private RoundedTextField txtPublisher;
    private RoundedTextField txtIsbn;
    private JComboBox<Category> cmbCategory;
    private RoundedTextField txtTotalCopies;
    private RoundedTextField txtAvailableCopies;
    private RoundedTextField txtYear;
    private JTextArea txtDescription;
    private JLabel lblCoverStatus;
    private String coverImagePath = null;

    public AddEditBookDialog(Window owner, Book bookToEdit) {
        super(owner, bookToEdit == null ? "Add New Book" : "Edit Book", ModalityType.APPLICATION_MODAL);
        this.bookService = new BookService();
        this.categoryService = new CategoryService();
        this.existingBook = bookToEdit;

        setSize(780, 560);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        if (existingBook != null) {
            populateFields(existingBook);
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeColors.BG_CARD);
        root.setBorder(new EmptyBorder(24, 28, 20, 28));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(existingBook == null ? "Add New Book" : "Edit Book Details");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Enter the details to " + (existingBook == null ? "add a new book to the library" : "update this book's record"));
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form Body: Two-column grid
        JPanel formGrid = new JPanel(new GridLayout(1, 2, 24, 0));
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(18, 0, 18, 0));

        // Left Column
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        txtTitle = new RoundedTextField("Enter book title", 20);
        leftCol.add(createFieldWrapper("Book Title *", txtTitle));
        leftCol.add(Box.createVerticalStrut(10));

        txtAuthor = new RoundedTextField("Enter author name", 20);
        leftCol.add(createFieldWrapper("Author *", txtAuthor));
        leftCol.add(Box.createVerticalStrut(10));

        cmbCategory = new JComboBox<>();
        cmbCategory.setFont(ThemeColors.FONT_BODY);
        cmbCategory.setBackground(ThemeColors.BG_INPUT);
        populateCategories();
        leftCol.add(createFieldWrapper("Category *", cmbCategory));
        leftCol.add(Box.createVerticalStrut(10));

        txtIsbn = new RoundedTextField("Enter ISBN (optional)", 20);
        leftCol.add(createFieldWrapper("ISBN", txtIsbn));
        leftCol.add(Box.createVerticalStrut(10));

        JPanel copiesPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        copiesPanel.setOpaque(false);
        txtTotalCopies = new RoundedTextField("e.g. 5", 8);
        txtTotalCopies.setText("1");
        txtAvailableCopies = new RoundedTextField("e.g. 5", 8);
        txtAvailableCopies.setText("1");

        copiesPanel.add(createFieldWrapper("Total Copies *", txtTotalCopies));
        copiesPanel.add(createFieldWrapper("Available Copies *", txtAvailableCopies));
        leftCol.add(copiesPanel);

        formGrid.add(leftCol);

        // Right Column
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        JPanel pubYearRow = new JPanel(new GridLayout(1, 2, 10, 0));
        pubYearRow.setOpaque(false);
        txtPublisher = new RoundedTextField("Publisher name", 12);
        txtYear = new RoundedTextField("e.g. 2024", 6);
        pubYearRow.add(createFieldWrapper("Publisher", txtPublisher));
        pubYearRow.add(createFieldWrapper("Publication Year", txtYear));
        rightCol.add(pubYearRow);
        rightCol.add(Box.createVerticalStrut(10));

        // Description
        JPanel descWrapper = new JPanel(new BorderLayout(0, 4));
        descWrapper.setOpaque(false);
        JLabel lblDesc = new JLabel("Description");
        lblDesc.setFont(ThemeColors.FONT_BODY_BOLD);
        lblDesc.setForeground(ThemeColors.TEXT_DARK);
        descWrapper.add(lblDesc, BorderLayout.NORTH);

        txtDescription = new JTextArea(4, 20);
        txtDescription.setFont(ThemeColors.FONT_BODY);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        descWrapper.add(descScroll, BorderLayout.CENTER);
        rightCol.add(descWrapper);
        rightCol.add(Box.createVerticalStrut(12));

        // Cover image upload box
        RoundedPanel uploadBox = new RoundedPanel(10, ThemeColors.BG_INPUT, ThemeColors.BORDER);
        uploadBox.setLayout(new BorderLayout());
        uploadBox.setBorder(new EmptyBorder(12, 12, 12, 12));
        uploadBox.setPreferredSize(new Dimension(0, 80));

        JPanel uploadCenter = new JPanel(new GridBagLayout());
        uploadCenter.setOpaque(false);

        lblCoverStatus = new JLabel("Click to select cover image (Optional)");
        lblCoverStatus.setFont(ThemeColors.FONT_SUBTEXT);
        lblCoverStatus.setForeground(ThemeColors.TEXT_MUTED);

        RoundedButton btnChoose = new RoundedButton("Browse File", true);
        btnChoose.setPreferredSize(new Dimension(100, 28));
        btnChoose.addActionListener(e -> chooseCoverImage());

        uploadBox.add(lblCoverStatus, BorderLayout.CENTER);
        uploadBox.add(btnChoose, BorderLayout.EAST);

        JPanel coverWrapper = new JPanel(new BorderLayout(0, 4));
        coverWrapper.setOpaque(false);
        JLabel lblCover = new JLabel("Cover Image");
        lblCover.setFont(ThemeColors.FONT_BODY_BOLD);
        lblCover.setForeground(ThemeColors.TEXT_DARK);
        coverWrapper.add(lblCover, BorderLayout.NORTH);
        coverWrapper.add(uploadBox, BorderLayout.CENTER);

        rightCol.add(coverWrapper);

        formGrid.add(rightCol);
        root.add(formGrid, BorderLayout.CENTER);

        // Buttons Bar on South
        JPanel buttonsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonsBar.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Cancel", true);
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dispose());
        buttonsBar.add(btnCancel);

        RoundedButton btnSave = new RoundedButton(existingBook == null ? "Add Book" : "Save Changes");
        btnSave.setPreferredSize(new Dimension(130, 38));
        btnSave.addActionListener(e -> saveAction());
        buttonsBar.add(btnSave);

        root.add(buttonsBar, BorderLayout.SOUTH);
        setContentPane(root);
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

    private void populateCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            cmbCategory.removeAllItems();
            for (Category c : categories) {
                cmbCategory.addItem(c);
            }
        } catch (Exception e) {
            System.err.println("Could not load categories: " + e.getMessage());
        }
    }

    private void chooseCoverImage() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            coverImagePath = chooser.getSelectedFile().getAbsolutePath();
            lblCoverStatus.setText(chooser.getSelectedFile().getName());
        }
    }

    private void populateFields(Book book) {
        txtTitle.setText(book.getTitle());
        txtAuthor.setText(book.getAuthor());
        txtPublisher.setText(book.getPublisher() != null ? book.getPublisher() : "");
        txtIsbn.setText(book.getIsbn() != null ? book.getIsbn() : "");
        txtTotalCopies.setText(String.valueOf(book.getTotalCopies()));
        txtAvailableCopies.setText(String.valueOf(book.getAvailableCopies()));
        if (book.getPublicationYear() != null) {
            txtYear.setText(String.valueOf(book.getPublicationYear()));
        }
        txtDescription.setText(book.getDescription() != null ? book.getDescription() : "");
        int checkedOutCopies = book.getTotalCopies() - book.getAvailableCopies();
        txtAvailableCopies.setEditable(false);
        txtAvailableCopies.setToolTipText("Available copies are calculated from total copies and current loans.");
        txtTotalCopies.getDocument().addDocumentListener(new DocumentListener() {
            private void updateAvailableCopies() {
                try {
                    int totalCopies = Integer.parseInt(txtTotalCopies.getText().trim());
                    if (totalCopies >= checkedOutCopies) {
                        txtAvailableCopies.setText(String.valueOf(totalCopies - checkedOutCopies));
                    }
                } catch (NumberFormatException ignored) {
                    // Keep the last calculated value until the total is a valid number.
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateAvailableCopies();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateAvailableCopies();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateAvailableCopies();
            }
        });

        for (int i = 0; i < cmbCategory.getItemCount(); i++) {
            Category c = cmbCategory.getItemAt(i);
            if (c.getCategoryId() == book.getCategoryId()) {
                cmbCategory.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveAction() {
        try {
            String title = txtTitle.getText().trim();
            String author = txtAuthor.getText().trim();
            String publisher = txtPublisher.getText().trim();
            String isbn = txtIsbn.getText().trim();
            String desc = txtDescription.getText().trim();

            int total = Integer.parseInt(txtTotalCopies.getText().trim());
            int available = Integer.parseInt(txtAvailableCopies.getText().trim());

            Integer year = null;
            if (!txtYear.getText().trim().isEmpty()) {
                year = Integer.parseInt(txtYear.getText().trim());
            }

            Category cat = (Category) cmbCategory.getSelectedItem();
            if (cat == null) {
                throw new IllegalArgumentException("Select a book category.");
            }
            int catId = cat.getCategoryId();

            Book b = (existingBook != null) ? existingBook : new Book();
            b.setTitle(title);
            b.setAuthor(author);
            b.setPublisher(publisher);
            b.setIsbn(isbn.isEmpty() ? null : isbn);
            b.setCategoryId(catId);
            b.setDescription(desc);
            b.setTotalCopies(total);
            b.setAvailableCopies(available);
            b.setPublicationYear(year);
            if (coverImagePath != null) {
                b.setCoverImage(coverImagePath);
            }

            bookService.saveBook(b);
            saved = true;
            JOptionPane.showMessageDialog(this,
                    existingBook == null ? "Book added successfully!" : "Book details updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Copies and year must be valid numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save book: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
