package com.library.ui.panels;

import com.library.model.Member;
import com.library.service.MemberService;
import com.library.ui.components.RoundedButton;
import com.library.ui.components.RoundedTextField;
import com.library.ui.theme.ThemeColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddEditMemberDialog extends JDialog {

    private final MemberService memberService;
    private final Member existingMember;
    private boolean saved = false;

    private RoundedTextField txtName;
    private RoundedTextField txtEmail;
    private RoundedTextField txtPhone;
    private JComboBox<String> cmbType;
    private JComboBox<String> cmbStatus;

    public AddEditMemberDialog(Window owner, Member memberToEdit) {
        super(owner, memberToEdit == null ? "Register New Member" : "Edit Member Details", ModalityType.APPLICATION_MODAL);
        this.memberService = new MemberService();
        this.existingMember = memberToEdit;

        setSize(480, 480);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        if (existingMember != null) {
            populateFields(existingMember);
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

        JLabel lblTitle = new JLabel(existingMember == null ? "Register New Member" : "Edit Member");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Enter membership details below");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 0, 16, 0));

        txtName = new RoundedTextField("Enter full name", 20);
        form.add(createFieldWrapper("Full Name *", txtName));
        form.add(Box.createVerticalStrut(10));

        txtEmail = new RoundedTextField("member@domain.com", 20);
        form.add(createFieldWrapper("Email Address *", txtEmail));
        form.add(Box.createVerticalStrut(10));

        txtPhone = new RoundedTextField("e.g. 9876543210", 20);
        form.add(createFieldWrapper("Phone Number", txtPhone));
        form.add(Box.createVerticalStrut(10));

        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);

        cmbType = new JComboBox<>(new String[]{"Student", "Faculty", "Staff"});
        cmbType.setFont(ThemeColors.FONT_BODY);
        row.add(createFieldWrapper("Membership Type *", cmbType));

        cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        cmbStatus.setFont(ThemeColors.FONT_BODY);
        row.add(createFieldWrapper("Status", cmbStatus));

        form.add(row);
        root.add(form, BorderLayout.CENTER);

        // Buttons Bar
        JPanel buttonsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonsBar.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Cancel", true);
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnCancel.addActionListener(e -> dispose());
        buttonsBar.add(btnCancel);

        RoundedButton btnSave = new RoundedButton(existingMember == null ? "Register Member" : "Save Changes");
        btnSave.setPreferredSize(new Dimension(140, 36));
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

    private void populateFields(Member m) {
        txtName.setText(m.getName());
        txtEmail.setText(m.getEmail());
        txtPhone.setText(m.getPhone() != null ? m.getPhone() : "");
        cmbType.setSelectedItem(m.getMembershipType());
        cmbStatus.setSelectedItem(m.getStatus());
    }

    private void saveAction() {
        try {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String type = (String) cmbType.getSelectedItem();
            String status = (String) cmbStatus.getSelectedItem();

            Member m = (existingMember != null) ? existingMember : new Member();
            m.setName(name);
            m.setEmail(email);
            m.setPhone(phone);
            m.setMembershipType(type);
            m.setStatus(status);

            memberService.saveMember(m);
            saved = true;
            JOptionPane.showMessageDialog(this,
                    existingMember == null ? "Member registered successfully!" : "Member details updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
