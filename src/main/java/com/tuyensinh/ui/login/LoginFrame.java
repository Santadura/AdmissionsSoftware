package com.tuyensinh.ui.login;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.tuyensinh.entity.User;
import com.tuyensinh.service.UserService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;
import com.tuyensinh.ui.dashboard.DashboardFrame;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private UserService userService;

    public LoginFrame() {
        userService = new UserService();

        setTitle("Đăng nhập hệ thống tuyển sinh");
        setSize(640, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(AppColor.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel loginCard = new JPanel(new BorderLayout());
        loginCard.setBackground(AppColor.SURFACE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(30, 35, 30, 35)
        ));
        loginCard.setPreferredSize(new Dimension(560, 300));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("HỆ THỐNG TUYỂN SINH");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(AppColor.PRIMARY);

        JLabel lblSubTitle = new JLabel("Đăng nhập trang quản trị");
        lblSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubTitle.setForeground(AppColor.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(lblSubTitle);

        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setOpaque(false);
        formWrapper.setBorder(new EmptyBorder(25, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblUsername.setForeground(AppColor.TEXT_PRIMARY);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtUsername.setPreferredSize(new Dimension(260, 38));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblPassword.setForeground(AppColor.TEXT_PRIMARY);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtPassword.setPreferredSize(new Dimension(260, 38));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        formWrapper.add(lblUsername, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        formWrapper.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        formWrapper.add(lblPassword, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        formWrapper.add(txtPassword, gbc);

        btnLogin = new RoundedButton("Đăng nhập", AppColor.PRIMARY, AppColor.PRIMARY_DARK, 20);
        btnLogin.setPreferredSize(new Dimension(180, 44));
        btnLogin.addActionListener(e -> handleLogin());

        txtPassword.addActionListener(e -> handleLogin());
        txtUsername.addActionListener(e -> handleLogin());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnLogin);

        loginCard.add(titlePanel, BorderLayout.NORTH);
        loginCard.add(formWrapper, BorderLayout.CENTER);
        loginCard.add(bottomPanel, BorderLayout.SOUTH);

        root.add(loginCard);
        setContentPane(root);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        try {
            User user = userService.loginAdmin(username, password);
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công: " + user.getUsername());
            new DashboardFrame().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Đăng nhập thất bại",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}