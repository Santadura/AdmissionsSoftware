package com.tuyensinh.ui.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.tuyensinh.entity.User;
import com.tuyensinh.service.UserService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

public class UserManagementPanel extends JPanel {
    private JTable tableUsers;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnChangePassword;
    private JButton btnToggleStatus;
    private JButton btnRefresh;

    private UserService userService;

    public UserManagementPanel() {
        userService = new UserService();

        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initTop();
        initTable();
        initBottomActions();
        loadUsers();
    }

    private void initTop() {
        JLabel lblTitle = new JLabel("Quản lý người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);

        JPanel topWrapper = new JPanel(new BorderLayout(10, 15));
        topWrapper.setOpaque(false);
        topWrapper.add(lblTitle, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(AppColor.SURFACE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(12, 12, 12, 12)));

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(220, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(5, 10, 5, 10)));

        btnRefresh = new RoundedButton("Làm mới", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadUsers();
        });

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);

        topWrapper.add(searchPanel, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"ID", "Username", "Họ và tên", "Quyền", "Trạng thái"};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        tableUsers = new JTable(model);
        tableUsers.setRowHeight(32);
        tableUsers.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableUsers.setSelectionBackground(new Color(227, 242, 253));
        tableUsers.setGridColor(AppColor.BORDER);
        tableUsers.setShowVerticalLines(false);

        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"ADMIN", "USER"});
        DefaultCellEditor roleEditor = new DefaultCellEditor(roleComboBox);
        roleEditor.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                int row = tableUsers.getSelectedRow();
                if (row >= 0) {
                    try {
                        Integer userId = Integer.parseInt(model.getValueAt(row, 0).toString());
                        String newRole = model.getValueAt(row, 3).toString();
                        userService.updateRole(userId, newRole);
                        loadUsers();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(UserManagementPanel.this, ex.getMessage());
                    }
                }
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
            }
        });
        tableUsers.getColumnModel().getColumn(3).setCellEditor(roleEditor);

        JTableHeader header = tableUsers.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        JScrollPane scrollPane = new JScrollPane(tableUsers);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));

        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);
    }

    private void initBottomActions() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnAdd = new RoundedButton("Thêm admin", new Color(67, 160, 71), new Color(46, 125, 50));
        btnEdit = new RoundedButton("Sửa thông tin", new Color(251, 140, 0), new Color(239, 108, 0));
        btnChangePassword = new RoundedButton("Đổi mật khẩu", new Color(142, 36, 170), new Color(106, 27, 154));
        btnToggleStatus = new RoundedButton("Khóa/Mở khóa", AppColor.PRIMARY, AppColor.PRIMARY_DARK);

        btnAdd.addActionListener(e -> openAddDialog());
        btnEdit.addActionListener(e -> openEditDialog());
        btnChangePassword.addActionListener(e -> openChangePasswordDialog());
        btnToggleStatus.addActionListener(e -> toggleUserStatus());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnChangePassword);
        buttonPanel.add(btnToggleStatus);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        List<Object[]> users = userService.getAllUsersForTable();
        model.setRowCount(0);

        for (Object[] row : users) {
            String statusText;
            Object enabledValue = row[4];

            if (enabledValue instanceof Boolean) {
                statusText = ((Boolean) enabledValue) ? "Đang hoạt động" : "Đã khóa";
            } else {
                statusText = ((Number) enabledValue).intValue() == 1 ? "Đang hoạt động" : "Đã khóa";
            }

            model.addRow(new Object[]{
                    row[0],
                    row[1],
                    row[2],
                    row[3],
                    statusText
            });
        }
    }

    private Integer getSelectedUserId() {
        int selectedRow = tableUsers.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        return Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
    }

    private void toggleUserStatus() {
        Integer userId = getSelectedUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng.");
            return;
        }

        try {
            User oldUser = userService.getUserById(userId);
            if (oldUser == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng.");
                return;
            }

            boolean oldEnabled = oldUser.isEnabled();
            userService.toggleStatus(userId);
            loadUsers();

            JOptionPane.showMessageDialog(this,
                    oldEnabled ? "Đã khóa tài khoản." : "Đã mở khóa tài khoản.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Thêm admin",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(AppColor.SURFACE);

        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirmPassword = new JPasswordField();
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Đã khóa"});

        formPanel.add(new JLabel("Username:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Mật khẩu:"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel("Xác nhận mật khẩu:"));
        formPanel.add(txtConfirmPassword);
        formPanel.add(new JLabel("Trạng thái:"));
        formPanel.add(cboStatus);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnCancel = new RoundedButton("Hủy", Color.GRAY, Color.DARK_GRAY);

        btnSave.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());
            boolean enabled = cboStatus.getSelectedItem().toString().equals("Đang hoạt động");

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu xác nhận không khớp.");
                return;
            }

            try {
                userService.addAdmin(username, password, enabled);
                loadUsers();
                JOptionPane.showMessageDialog(dialog, "Thêm admin thành công.");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        actionPanel.add(btnSave);
        actionPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(actionPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        Integer userId = getSelectedUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để sửa.");
            return;
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng.");
            return;
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Sửa thông tin người dùng",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(AppColor.SURFACE);

        JTextField txtUsername = new JTextField(user.getUsername());
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Đã khóa"});
        cboStatus.setSelectedItem(user.isEnabled() ? "Đang hoạt động" : "Đã khóa");

        formPanel.add(new JLabel("Username:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Trạng thái:"));
        formPanel.add(cboStatus);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnCancel = new RoundedButton("Hủy", Color.GRAY, Color.DARK_GRAY);

        btnSave.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            boolean enabled = cboStatus.getSelectedItem().toString().equals("Đang hoạt động");

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username không được để trống.");
                return;
            }

            try {
                userService.updateUser(userId, username, enabled);
                loadUsers();
                JOptionPane.showMessageDialog(dialog, "Cập nhật thông tin thành công.");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        actionPanel.add(btnSave);
        actionPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(actionPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openChangePasswordDialog() {
        Integer userId = getSelectedUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để đổi mật khẩu.");
            return;
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng.");
            return;
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Đổi mật khẩu",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(420, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(AppColor.SURFACE);

        JLabel lblUsername = new JLabel(user.getUsername());
        JPasswordField txtNewPassword = new JPasswordField();
        JPasswordField txtConfirmPassword = new JPasswordField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(lblUsername);
        formPanel.add(new JLabel("Mật khẩu mới:"));
        formPanel.add(txtNewPassword);
        formPanel.add(new JLabel("Xác nhận mật khẩu:"));
        formPanel.add(txtConfirmPassword);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnCancel = new RoundedButton("Hủy", Color.GRAY, Color.DARK_GRAY);

        btnSave.addActionListener(e -> {
            String newPassword = new String(txtNewPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu xác nhận không khớp.");
                return;
            }

            try {
                userService.changePassword(userId, newPassword);
                JOptionPane.showMessageDialog(dialog, "Đổi mật khẩu thành công.");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        actionPanel.add(btnSave);
        actionPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(actionPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}