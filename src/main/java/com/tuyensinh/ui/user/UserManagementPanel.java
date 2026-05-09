package com.tuyensinh.ui.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

public class UserManagementPanel extends JPanel {
    private JTable tableUsers;
    private JTextField txtSearch;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnChangePassword;
    private JButton btnToggleStatus;
    private JButton btnRefresh;

    public UserManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

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
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(220, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));

        btnRefresh = new RoundedButton("Làm mới", AppColor.PRIMARY, AppColor.PRIMARY_DARK);

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);

        topWrapper.add(searchPanel, BorderLayout.SOUTH);

        String[] columns = {"ID", "Username", "Họ tên", "Quyền", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        tableUsers = new JTable(model);
        tableUsers.setRowHeight(32);
        tableUsers.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableUsers.setSelectionBackground(new Color(227, 242, 253));
        tableUsers.setGridColor(AppColor.BORDER);
        tableUsers.setShowVerticalLines(false);

        model.addRow(new Object[]{1, "admin", "Quản trị viên", "admin", "Đang hoạt động"});
        model.addRow(new Object[]{2, "user01", "Nguyễn Văn A", "user", "Đã khóa"});

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
                new EmptyBorder(10, 10, 10, 10)
        ));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnAdd = new RoundedButton("Thêm", new Color(67, 160, 71), new Color(46, 125, 50));
        btnEdit = new RoundedButton("Sửa", new Color(251, 140, 0), new Color(239, 108, 0));
        btnChangePassword = new RoundedButton("Đổi mật khẩu", new Color(142, 36, 170), new Color(106, 27, 154));
        btnToggleStatus = new RoundedButton("Enable/Disable", AppColor.PRIMARY, AppColor.PRIMARY_DARK);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnChangePassword);
        buttonPanel.add(btnToggleStatus);

        add(topWrapper, BorderLayout.NORTH);
        add(centerCard, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}