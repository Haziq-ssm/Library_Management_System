package com.library.ui.panels;

import com.library.model.LibraryTransaction;
import com.library.service.FineService;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedPanel;
import com.library.ui.components.RoundedTextField;
import com.library.ui.theme.ThemeColors;
import com.library.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;

public class PayFineDialog extends JDialog {

    private final FineService fineService;
    private final LibraryTransaction transaction;
    private boolean paymentSuccess = false;

    private RoundedTextField txtAmount;
    private JComboBox<String> cmbMethod;
    private RoundedTextField txtDate;
    private RoundedTextField txtNotes;

    public PayFineDialog(Window owner, LibraryTransaction transaction) {
        super(owner, "Pay Fine / Settle Balance", ModalityType.APPLICATION_MODAL);
        this.fineService = new FineService();
        this.transaction = transaction;

        setSize(460, 480);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeColors.BG_CARD);
        root.setBorder(new EmptyBorder(22, 24, 20, 24));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Fine Payment");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Record fine settlement for Transaction #" + transaction.getTransId());
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(3));
        header.add(lblSub);
        root.add(header, BorderLayout.NORTH);

        // Body Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Info Summary Card
        RoundedPanel infoBox = new RoundedPanel(10, ThemeColors.PRIMARY_LIGHT, ThemeColors.PRIMARY_BORDER);
        infoBox.setLayout(new GridLayout(3, 2, 8, 4));
        infoBox.setBorder(new EmptyBorder(10, 14, 10, 14));

        infoBox.add(createSummaryLabel("Member:", ThemeColors.TEXT_MUTED, false));
        infoBox.add(createSummaryLabel(transaction.getMemberName() + " (" + transaction.getMemberCode() + ")", ThemeColors.TEXT_DARK, true));

        infoBox.add(createSummaryLabel("Book Title:", ThemeColors.TEXT_MUTED, false));
        infoBox.add(createSummaryLabel(transaction.getBookTitle(), ThemeColors.TEXT_DARK, true));

        infoBox.add(createSummaryLabel("Outstanding Balance:", ThemeColors.TEXT_MUTED, false));
        infoBox.add(createSummaryLabel(String.format("₹%.2f", transaction.getFineBalance()), ThemeColors.PRIMARY, true));
        form.add(infoBox);
        form.add(Box.createVerticalStrut(14));

        // Payment Amount
        txtAmount = new RoundedTextField(10);
        txtAmount.setText(String.format("%.2f", transaction.getFineBalance()));
        form.add(createFieldWrapper("Amount to Pay (₹) *", txtAmount));
        form.add(Box.createVerticalStrut(10));

        // Payment Method
        cmbMethod = new JComboBox<>(new String[]{"Cash", "Card", "UPI", "Online Transfer"});
        cmbMethod.setFont(ThemeColors.FONT_BODY);
        form.add(createFieldWrapper("Payment Method *", cmbMethod));
        form.add(Box.createVerticalStrut(10));

        // Payment Date
        txtDate = new RoundedTextField(10);
        txtDate.setText(DateUtil.formatDisplay(DateUtil.today()));
        form.add(createFieldWrapper("Payment Date", txtDate));
        form.add(Box.createVerticalStrut(10));

        // Notes
        txtNotes = new RoundedTextField("e.g. Receipt #4829, UPI txn ID...", 20);
        form.add(createFieldWrapper("Notes / Remarks", txtNotes));
        root.add(form, BorderLayout.CENTER);

        // Buttons Bar
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Cancel", true);
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.addActionListener(e -> dispose());
        btnRow.add(btnCancel);

        RoundedButton btnPay = new RoundedButton("Record Payment");
        btnPay.setPreferredSize(new Dimension(140, 36));
        btnPay.addActionListener(e -> processPayment());
        btnRow.add(btnPay);

        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JLabel createSummaryLabel(String text, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(bold ? ThemeColors.FONT_BODY_BOLD : ThemeColors.FONT_SUBTEXT);
        l.setForeground(color);
        return l;
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

    private void processPayment() {
        try {
            double amt = Double.parseDouble(txtAmount.getText().trim());
            if (!Double.isFinite(amt) || amt <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be a valid number greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (amt > transaction.getFineBalance() + 0.005) {
                JOptionPane.showMessageDialog(this,
                        String.format("Amount cannot exceed the outstanding balance of ₹%.2f.", transaction.getFineBalance()),
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date payDate = DateUtil.parseDisplay(txtDate.getText().trim());
            if (payDate == null && !txtDate.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a valid payment date (DD-MM-YYYY).",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (payDate == null) {
                payDate = DateUtil.today();
            }

            String method = (String) cmbMethod.getSelectedItem();
            String notes = txtNotes.getText().trim();

            fineService.recordPayment(transaction.getTransId(), transaction.getMemberId(), amt, payDate, method, notes, "admin");
            paymentSuccess = true;
            JOptionPane.showMessageDialog(this,
                    "Fine payment of ₹" + String.format("%.2f", amt) + " recorded successfully!",
                    "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Payment error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isPaymentSuccess() {
        return paymentSuccess;
    }
}
