package com.library.ui.panels;

import com.library.model.Book;
import com.library.model.LibraryTransaction;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.LibraryService;
import com.library.service.MemberService;
import com.library.service.SettingService;
import com.library.ui.DashboardFrame;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedPanel;
import com.library.ui.components.RoundedTextField;
import com.library.ui.theme.ThemeColors;
import com.library.ui.theme.UIUtils;
import com.library.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class IssueReturnPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final LibraryService libraryService;
    private final BookService bookService;
    private final MemberService memberService;
    private final SettingService settingService;

    // Mode Toggle
    private boolean isIssueMode = true;
    private JButton btnTabIssue;
    private JButton btnTabReturn;
    private JPanel formContainer;

    // Issue Mode Form Components
    private JComboBox<Member> cmbIssueMember;
    private JComboBox<Book> cmbIssueBook;
    private RoundedTextField txtIssueDate;
    private RoundedTextField txtDueDate;
    private RoundedTextField txtIssueNotes;

    // Return Mode Form Components
    private JComboBox<LibraryTransaction> cmbReturnTransaction;
    private RoundedTextField txtReturnDate;
    private JLabel lblOverdueDays;
    private JLabel lblCalculatedFine;
    private JCheckBox chkPayFineNow;
    private JComboBox<String> cmbPaymentMethod;
    private RoundedTextField txtPaymentNotes;

    // Right Side: Member Details Card
    private JLabel lblDetailAvatar;
    private JLabel lblDetailName;
    private JLabel lblDetailId;
    private JLabel lblDetailType;
    private JLabel lblDetailEmail;
    private JLabel lblDetailPhone;
    private JLabel lblDetailBooksIssued;

    private List<Member> membersList = new ArrayList<>();
    private List<Book> availableBooksList = new ArrayList<>();
    private List<LibraryTransaction> activeLoansList = new ArrayList<>();

    public IssueReturnPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.libraryService = new LibraryService();
        this.bookService = new BookService();
        this.memberService = new MemberService();
        this.settingService = new SettingService();

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadData();
    }

    private void initUI() {
        // Top Container: Header & Mode Tabs
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header Title
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Issue / Return Book");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Manage book issue and return transactions");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(lblSub);
        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(18));

        // Mode Switch Pills ("Issue Book" | "Return Book")
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        togglePanel.setOpaque(false);

        btnTabIssue = createModeTab("Issue Book", true);
        btnTabIssue.addActionListener(e -> switchMode(true));

        btnTabReturn = createModeTab("Return Book", false);
        btnTabReturn.addActionListener(e -> switchMode(false));

        togglePanel.add(btnTabIssue);
        togglePanel.add(btnTabReturn);
        topContainer.add(togglePanel);
        topContainer.add(Box.createVerticalStrut(18));

        add(topContainer, BorderLayout.NORTH);

        // Center Content: Dual Column (Form on left 62%, Member Details on right 38%)
        JPanel centerGrid = new JPanel(new GridBagLayout());
        centerGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Form Card
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 10);
        formContainer = new JPanel(new CardLayout());
        formContainer.setOpaque(false);
        formContainer.add(createIssueFormCard(), "ISSUE");
        formContainer.add(createReturnFormCard(), "RETURN");
        centerGrid.add(formContainer, gbc);

        // Right Member Details Card
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 10, 0, 0);
        centerGrid.add(createMemberDetailsCard(), gbc);

        add(centerGrid, BorderLayout.CENTER);
    }

    private JButton createModeTab(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeColors.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(130, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateModeTabStyle(btn, active);
        return btn;
    }

    private void updateModeTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(ThemeColors.PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
        } else {
            btn.setBackground(new Color(243, 244, 246));
            btn.setForeground(ThemeColors.TEXT_MUTED);
            btn.setOpaque(true);
        }
    }

    private void switchMode(boolean issueMode) {
        this.isIssueMode = issueMode;
        updateModeTabStyle(btnTabIssue, issueMode);
        updateModeTabStyle(btnTabReturn, !issueMode);

        CardLayout cl = (CardLayout) formContainer.getLayout();
        if (issueMode) {
            cl.show(formContainer, "ISSUE");
            onSelectedIssueMemberChanged();
        } else {
            cl.show(formContainer, "RETURN");
            onSelectedReturnTransactionChanged();
        }
    }

    private RoundedPanel createIssueFormCard() {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE, ThemeColors.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Select Member
        cmbIssueMember = new JComboBox<>();
        cmbIssueMember.setFont(ThemeColors.FONT_BODY);
        cmbIssueMember.setBackground(ThemeColors.BG_INPUT);
        cmbIssueMember.addActionListener(e -> onSelectedIssueMemberChanged());
        form.add(createFieldWrapper("Select Member *", cmbIssueMember));
        form.add(Box.createVerticalStrut(14));

        // Select Book
        cmbIssueBook = new JComboBox<>();
        cmbIssueBook.setFont(ThemeColors.FONT_BODY);
        cmbIssueBook.setBackground(ThemeColors.BG_INPUT);
        form.add(createFieldWrapper("Select Book *", cmbIssueBook));
        form.add(Box.createVerticalStrut(14));

        // Issue Date and Due Date row
        JPanel datesRow = new JPanel(new GridLayout(1, 2, 14, 0));
        datesRow.setOpaque(false);

        Date today = DateUtil.today();
        int loanDays = settingService.getLoanPeriodDays();
        Date dueDate = DateUtil.addDays(today, loanDays);

        txtIssueDate = new RoundedTextField(10);
        txtIssueDate.setText(DateUtil.formatDisplay(today));

        txtDueDate = new RoundedTextField(10);
        txtDueDate.setText(DateUtil.formatDisplay(dueDate));

        datesRow.add(createFieldWrapper("Issue Date", txtIssueDate));
        datesRow.add(createFieldWrapper("Due Date", txtDueDate));
        form.add(datesRow);
        form.add(Box.createVerticalStrut(14));

        txtIssueNotes = new RoundedTextField("Optional notes for this issue...", 20);
        form.add(createFieldWrapper("Notes", txtIssueNotes));
        form.add(Box.createVerticalStrut(24));

        // Issue Button
        RoundedButton btnSubmitIssue = new RoundedButton("Issue Book");
        btnSubmitIssue.setPreferredSize(new Dimension(140, 42));
        btnSubmitIssue.addActionListener(e -> processIssueBook());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnSubmitIssue);
        form.add(btnRow);

        card.add(form, BorderLayout.NORTH);
        return card;
    }

    private RoundedPanel createReturnFormCard() {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE, ThemeColors.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Select Active Transaction
        cmbReturnTransaction = new JComboBox<>();
        cmbReturnTransaction.setFont(ThemeColors.FONT_BODY);
        cmbReturnTransaction.setBackground(ThemeColors.BG_INPUT);
        cmbReturnTransaction.addActionListener(e -> onSelectedReturnTransactionChanged());
        form.add(createFieldWrapper("Select Active Borrowed Book *", cmbReturnTransaction));
        form.add(Box.createVerticalStrut(14));

        // Return Date Field
        txtReturnDate = new RoundedTextField(10);
        txtReturnDate.setText(DateUtil.formatDisplay(DateUtil.today()));
        txtReturnDate.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateReturnCalculations();
            }
        });
        form.add(createFieldWrapper("Return Date", txtReturnDate));
        form.add(Box.createVerticalStrut(14));

        // Overdue & Fine Details Card
        RoundedPanel fineInfoBox = new RoundedPanel(10, ThemeColors.PRIMARY_LIGHT, ThemeColors.PRIMARY_BORDER);
        fineInfoBox.setLayout(new GridLayout(2, 2, 10, 6));
        fineInfoBox.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblOverdueTitle = new JLabel("Days Overdue:");
        lblOverdueTitle.setFont(ThemeColors.FONT_SUBTEXT);
        lblOverdueDays = new JLabel("0 days");
        lblOverdueDays.setFont(ThemeColors.FONT_BODY_BOLD);
        lblOverdueDays.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblFineTitle = new JLabel("Calculated Overdue Fine:");
        lblFineTitle.setFont(ThemeColors.FONT_SUBTEXT);
        lblCalculatedFine = new JLabel("₹0.00");
        lblCalculatedFine.setFont(ThemeColors.FONT_BODY_BOLD);
        lblCalculatedFine.setForeground(ThemeColors.PRIMARY);

        fineInfoBox.add(lblOverdueTitle);
        fineInfoBox.add(lblOverdueDays);
        fineInfoBox.add(lblFineTitle);
        fineInfoBox.add(lblCalculatedFine);
        form.add(fineInfoBox);
        form.add(Box.createVerticalStrut(14));

        // Payment check
        chkPayFineNow = new JCheckBox("Settle / Pay fine right now");
        chkPayFineNow.setFont(ThemeColors.FONT_BODY_BOLD);
        chkPayFineNow.setForeground(ThemeColors.TEXT_DARK);
        chkPayFineNow.setOpaque(false);
        chkPayFineNow.setSelected(true);
        form.add(chkPayFineNow);
        form.add(Box.createVerticalStrut(8));

        JPanel payRow = new JPanel(new GridLayout(1, 2, 12, 0));
        payRow.setOpaque(false);

        cmbPaymentMethod = new JComboBox<>(new String[]{"Cash", "Card", "UPI", "Online"});
        cmbPaymentMethod.setFont(ThemeColors.FONT_BODY);
        payRow.add(createFieldWrapper("Payment Method", cmbPaymentMethod));

        txtPaymentNotes = new RoundedTextField("Payment reference / notes", 14);
        payRow.add(createFieldWrapper("Payment Notes", txtPaymentNotes));
        form.add(payRow);
        form.add(Box.createVerticalStrut(22));

        // Return Button
        RoundedButton btnSubmitReturn = new RoundedButton("Process Return");
        btnSubmitReturn.setPreferredSize(new Dimension(150, 42));
        btnSubmitReturn.addActionListener(e -> processReturnBook());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnSubmitReturn);
        form.add(btnRow);

        card.add(form, BorderLayout.NORTH);
        return card;
    }

    private RoundedPanel createMemberDetailsCard() {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE, ThemeColors.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel lblHeading = new JLabel("Member Details");
        lblHeading.setFont(ThemeColors.FONT_CARD_TITLE);
        lblHeading.setForeground(ThemeColors.TEXT_DARK);
        card.add(lblHeading, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Circular Avatar placeholder
        JPanel avatarCircle = new JPanel() {
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
        avatarCircle.setOpaque(false);
        avatarCircle.setPreferredSize(new Dimension(64, 64));
        avatarCircle.setMaximumSize(new Dimension(64, 64));
        avatarCircle.setLayout(new GridBagLayout());

        lblDetailAvatar = new JLabel(UIUtils.createIcon("user", 32, ThemeColors.PRIMARY));
        avatarCircle.add(lblDetailAvatar);

        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarWrapper.setOpaque(false);
        avatarWrapper.add(avatarCircle);
        content.add(avatarWrapper);
        content.add(Box.createVerticalStrut(14));

        // Detail rows
        lblDetailName = new JLabel("-");
        lblDetailId = new JLabel("-");
        lblDetailType = new JLabel("-");
        lblDetailEmail = new JLabel("-");
        lblDetailPhone = new JLabel("-");
        lblDetailBooksIssued = new JLabel("-");

        content.add(createDetailRow("Name:", lblDetailName));
        content.add(Box.createVerticalStrut(8));
        content.add(createDetailRow("Member ID:", lblDetailId));
        content.add(Box.createVerticalStrut(8));
        content.add(createDetailRow("Type:", lblDetailType));
        content.add(Box.createVerticalStrut(8));
        content.add(createDetailRow("Email:", lblDetailEmail));
        content.add(Box.createVerticalStrut(8));
        content.add(createDetailRow("Phone:", lblDetailPhone));
        content.add(Box.createVerticalStrut(8));
        content.add(createDetailRow("Books Issued:", lblDetailBooksIssued));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeColors.FONT_SUBTEXT);
        lbl.setForeground(ThemeColors.TEXT_MUTED);

        valueLabel.setFont(ThemeColors.FONT_BODY_BOLD);
        valueLabel.setForeground(ThemeColors.TEXT_DARK);

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
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

    public void loadData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                membersList = memberService.getAllMembers();
                availableBooksList = bookService.searchAndFilter(null, null, "Available", null);
                activeLoansList = libraryService.getActiveLoans();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(IssueReturnPanel.this,
                            "Loan data loading was interrupted.", "Loading Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(IssueReturnPanel.this,
                            "Could not load members, books, and active loans: " + cause.getMessage(),
                            "Loading Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Populate Issue Member dropdown
                cmbIssueMember.removeAllItems();
                for (Member m : membersList) {
                    if (m.isActive()) {
                        cmbIssueMember.addItem(m);
                    }
                }

                // Populate Issue Book dropdown
                cmbIssueBook.removeAllItems();
                for (Book b : availableBooksList) {
                    if (b.getAvailableCopies() > 0) {
                        cmbIssueBook.addItem(b);
                    }
                }

                // Populate Return Transaction dropdown
                cmbReturnTransaction.removeAllItems();
                for (LibraryTransaction t : activeLoansList) {
                    cmbReturnTransaction.addItem(t);
                }

                if (isIssueMode) {
                    onSelectedIssueMemberChanged();
                } else {
                    onSelectedReturnTransactionChanged();
                }
            }
        }.execute();
    }

    private void onSelectedIssueMemberChanged() {
        Member m = (Member) cmbIssueMember.getSelectedItem();
        if (m != null) {
            updateMemberDetails(m.getName(), m.getMemberCode(), m.getMembershipType(), m.getEmail(),
                    m.getPhone() != null ? m.getPhone() : "-", String.valueOf(m.getActiveLoansCount()));
        } else {
            clearMemberDetails();
        }
    }

    private void onSelectedReturnTransactionChanged() {
        LibraryTransaction t = (LibraryTransaction) cmbReturnTransaction.getSelectedItem();
        if (t != null) {
            updateMemberDetails(t.getMemberName(), t.getMemberCode(), "Borrower", t.getMemberEmail(),
                    "-", "1 active loan: " + t.getBookTitle());
            updateReturnCalculations();
        } else {
            clearMemberDetails();
            lblOverdueDays.setText("0 days");
            lblCalculatedFine.setText("₹0.00");
        }
    }

    private void updateReturnCalculations() {
        LibraryTransaction t = (LibraryTransaction) cmbReturnTransaction.getSelectedItem();
        if (t == null) return;

        Date retDate = DateUtil.parseDisplay(txtReturnDate.getText().trim());
        if (retDate == null) {
            lblOverdueDays.setText("Enter a valid date");
            lblCalculatedFine.setText("—");
            return;
        }

        int overdueDays = DateUtil.calculateOverdueDays(t.getDueDate(), retDate);
        double fineRate = settingService.getFinePerDay();
        double fine = overdueDays * fineRate;

        lblOverdueDays.setText(overdueDays + " day" + (overdueDays == 1 ? "" : "s"));
        lblCalculatedFine.setText(String.format("₹%.2f", fine));
    }

    private void updateMemberDetails(String name, String id, String type, String email, String phone, String books) {
        lblDetailName.setText(name);
        lblDetailId.setText(id);
        lblDetailType.setText(type);
        lblDetailEmail.setText(email);
        lblDetailPhone.setText(phone);
        lblDetailBooksIssued.setText(books);
    }

    private void clearMemberDetails() {
        lblDetailName.setText("-");
        lblDetailId.setText("-");
        lblDetailType.setText("-");
        lblDetailEmail.setText("-");
        lblDetailPhone.setText("-");
        lblDetailBooksIssued.setText("-");
    }

    private void processIssueBook() {
        try {
            Member m = (Member) cmbIssueMember.getSelectedItem();
            Book b = (Book) cmbIssueBook.getSelectedItem();

            if (m == null) {
                JOptionPane.showMessageDialog(this, "Please select a valid member.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (b == null) {
                JOptionPane.showMessageDialog(this, "Please select an available book.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date issueDate = DateUtil.parseDisplay(txtIssueDate.getText().trim());
            Date dueDate = DateUtil.parseDisplay(txtDueDate.getText().trim());
            if (issueDate == null || dueDate == null) {
                JOptionPane.showMessageDialog(this, "Enter valid issue and due dates (DD-MM-YYYY).",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!dueDate.after(issueDate)) {
                JOptionPane.showMessageDialog(this, "The due date must be after the issue date.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String notes = txtIssueNotes.getText().trim();

            libraryService.issueBook(b.getBookId(), m.getMemberId(), issueDate, dueDate, notes, "admin");

            JOptionPane.showMessageDialog(this,
                    "Book successfully issued!\n\nBook: " + b.getTitle() + "\nMember: " + m.getName() +
                            "\nDue Date: " + DateUtil.formatDisplay(dueDate),
                    "Transaction Success", JOptionPane.INFORMATION_MESSAGE);

            loadData();
            parentFrame.refreshDashboardStats();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Issue Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processReturnBook() {
        try {
            LibraryTransaction t = (LibraryTransaction) cmbReturnTransaction.getSelectedItem();
            if (t == null) {
                JOptionPane.showMessageDialog(this, "Please select an active transaction to return.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date retDate = DateUtil.parseDisplay(txtReturnDate.getText().trim());
            if (retDate == null) {
                JOptionPane.showMessageDialog(this, "Enter a valid return date (DD-MM-YYYY).",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (t.getIssueDate() != null && retDate.before(t.getIssueDate())) {
                JOptionPane.showMessageDialog(this, "The return date cannot be before the issue date.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int overdueDays = DateUtil.calculateOverdueDays(t.getDueDate(), retDate);
            double fineRate = settingService.getFinePerDay();
            double fine = overdueDays * fineRate;

            Double paymentAmount = null;
            String payMethod = null;
            if (fine > 0 && chkPayFineNow.isSelected()) {
                paymentAmount = fine;
                payMethod = (String) cmbPaymentMethod.getSelectedItem();
            }

            libraryService.returnBook(t.getTransId(), retDate, paymentAmount, payMethod, "admin");

            String msg = "Book returned successfully!\n\nBook: " + t.getBookTitle() +
                    "\nMember: " + t.getMemberName() +
                    (overdueDays > 0 ? "\nOverdue Days: " + overdueDays + "\nFine: ₹" + fine +
                            (paymentAmount != null ? " (Paid via " + payMethod + ")" : " (Unpaid)") : "");

            JOptionPane.showMessageDialog(this, msg, "Return Complete", JOptionPane.INFORMATION_MESSAGE);

            loadData();
            parentFrame.refreshDashboardStats();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Return Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
