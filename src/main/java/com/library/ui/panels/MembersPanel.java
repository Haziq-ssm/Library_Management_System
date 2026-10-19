package com.library.ui.panels;

import com.library.model.Member;
import com.library.service.MemberService;
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

public class MembersPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final MemberService memberService;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private List<Member> currentMembersList = new ArrayList<>();

    private RoundedTextField searchField;
    private String currentTypeFilter = "All Members";
    private String currentStatusFilter = null;
    private JButton[] filterTabs;

    public MembersPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.memberService = new MemberService();

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadMembersData();
    }

    private void initUI() {
        // Top Container: Header + Filters
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. Header with Add Member button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Members");
        lblTitle.setFont(ThemeColors.FONT_HEADER_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_DARK);

        JLabel lblSub = new JLabel("Manage library members");
        lblSub.setFont(ThemeColors.FONT_SUBTEXT);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(lblSub);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        RoundedButton btnAdd = new RoundedButton("+ Add Member");
        btnAdd.setPreferredSize(new Dimension(140, 38));
        btnAdd.addActionListener(e -> openAddMemberDialog());
        headerPanel.add(btnAdd, BorderLayout.EAST);

        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(18));

        // 2. Filter Pills + Search Field
        JPanel filterRow = new JPanel(new BorderLayout(15, 0));
        filterRow.setOpaque(false);

        JPanel pillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pillsPanel.setOpaque(false);

        String[] tabNames = {"All Members", "Students", "Faculty", "Staff", "Inactive"};
        filterTabs = new JButton[tabNames.length];
        for (int i = 0; i < tabNames.length; i++) {
            final String tabName = tabNames[i];
            filterTabs[i] = createFilterTab(tabName, i == 0);
            final int tabIdx = i;
            filterTabs[i].addActionListener(e -> {
                if ("Inactive".equalsIgnoreCase(tabName)) {
                    currentTypeFilter = "All Members";
                    currentStatusFilter = "Inactive";
                } else if ("Students".equalsIgnoreCase(tabName)) {
                    currentTypeFilter = "Student";
                    currentStatusFilter = null;
                } else if ("Faculty".equalsIgnoreCase(tabName)) {
                    currentTypeFilter = "Faculty";
                    currentStatusFilter = null;
                } else if ("Staff".equalsIgnoreCase(tabName)) {
                    currentTypeFilter = "Staff";
                    currentStatusFilter = null;
                } else {
                    currentTypeFilter = "All Members";
                    currentStatusFilter = null;
                }

                for (int j = 0; j < filterTabs.length; j++) {
                    updateFilterTabStyle(filterTabs[j], j == tabIdx);
                }
                loadMembersData();
            });
            pillsPanel.add(filterTabs[i]);
        }
        filterRow.add(pillsPanel, BorderLayout.WEST);

        searchField = new RoundedTextField("Search members by name, ID, email...", 22);
        searchField.setPreferredSize(new Dimension(280, 34));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadMembersData();
            }
        });
        filterRow.add(searchField, BorderLayout.EAST);

        topContainer.add(filterRow);
        topContainer.add(Box.createVerticalStrut(14));
        add(topContainer, BorderLayout.NORTH);

        // Center Table in Card
        RoundedPanel tableCard = new RoundedPanel(14, ThemeColors.BG_CARD, ThemeColors.BORDER);
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"#", "Member ID", "Name", "Type", "Email", "Phone", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        table = new ModernTable(tableModel);
        table.setBadgeColumn(6); // Status badge
        table.setActionColumn(7, new ModernTable.ActionCallback() {
            @Override
            public void onEdit(int row) {
                editMemberAtRow(row);
            }

            @Override
            public void onDelete(int row) {
                deleteMemberAtRow(row);
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);

        tableCard.add(ModernTable.createModernScrollPane(table), BorderLayout.CENTER);
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

    public void loadMembersData() {
        String kw = (searchField != null) ? searchField.getText().trim() : null;
        new SwingWorker<List<Member>, Void>() {
            @Override
            protected List<Member> doInBackground() throws Exception {
                return memberService.searchAndFilter(kw, currentTypeFilter, currentStatusFilter);
            }

            @Override
            protected void done() {
                try {
                    currentMembersList = get();
                    tableModel.setRowCount(0);
                    for (int i = 0; i < currentMembersList.size(); i++) {
                        Member m = currentMembersList.get(i);
                        tableModel.addRow(new Object[]{
                                (i + 1),
                                m.getMemberCode(),
                                m.getName(),
                                m.getMembershipType(),
                                m.getEmail(),
                                m.getPhone() != null ? m.getPhone() : "-",
                                m.getStatus(),
                                ""
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MembersPanel.this,
                            "Could not load members: " + e.getMessage(),
                            "Loading Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public void openAddMemberDialog() {
        AddEditMemberDialog dialog = new AddEditMemberDialog(parentFrame, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadMembersData();
            parentFrame.refreshDashboardStats();
        }
    }

    private void editMemberAtRow(int row) {
        if (row >= 0 && row < currentMembersList.size()) {
            Member m = currentMembersList.get(row);
            AddEditMemberDialog dialog = new AddEditMemberDialog(parentFrame, m);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadMembersData();
                parentFrame.refreshDashboardStats();
            }
        }
    }

    private void deleteMemberAtRow(int row) {
        if (row >= 0 && row < currentMembersList.size()) {
            Member m = currentMembersList.get(row);

            String[] options = {"Toggle Status (" + (m.isActive() ? "Deactivate" : "Activate") + ")", "Delete Member", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Manage member: " + m.getName() + " (" + m.getMemberCode() + ")",
                    "Member Options", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) {
                // Toggle status
                try {
                    memberService.toggleStatus(m.getMemberId(), m.getStatus());
                    loadMembersData();
                    parentFrame.refreshDashboardStats();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (choice == 1) {
                // Delete member
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Permanently delete member record for " + m.getName() + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        memberService.deleteMember(m.getMemberId());
                        JOptionPane.showMessageDialog(this, "Member deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadMembersData();
                        parentFrame.refreshDashboardStats();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Delete Member", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}
