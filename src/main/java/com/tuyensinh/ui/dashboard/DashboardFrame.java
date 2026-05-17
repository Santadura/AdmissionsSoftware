package com.tuyensinh.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;
import com.tuyensinh.ui.candidate.CandidateManagementPanel;
import com.tuyensinh.ui.conversion.ScoreConversionPanel;
import com.tuyensinh.ui.user.UserManagementPanel;
import com.tuyensinh.ui.major_combination.MajorCombinationPanel;

public class DashboardFrame extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public DashboardFrame() {
        setTitle("Phần mềm tuyển sinh - Admin");
        setSize(1300, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColor.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColor.PRIMARY);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblHeader = new JLabel("HỆ THỐNG TUYỂN SINH - ADMIN");
        lblHeader.setForeground(AppColor.TEXT_LIGHT);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel lblUser = new JLabel("Xin chào, Admin");
        lblUser.setForeground(AppColor.TEXT_LIGHT);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        header.add(lblHeader, BorderLayout.WEST);
        header.add(lblUser, BorderLayout.EAST);

        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setBackground(AppColor.SIDEBAR);
        sidebarWrapper.setBorder(new EmptyBorder(15, 15, 15, 15));
        sidebarWrapper.setPreferredSize(new Dimension(250, 0));

        JLabel lblMenu = new JLabel("DANH MỤC CHỨC NĂNG");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMenu.setForeground(AppColor.TEXT_PRIMARY);
        lblMenu.setBorder(new EmptyBorder(0, 5, 15, 5));

        JPanel sidebar = new JPanel(new GridLayout(9, 1, 0, 10));
        sidebar.setOpaque(false);

        String[] menuNames = {
                "Quản lý người dùng",
                "Quản lý thí sinh",
                "Quản lý ngành",
                "Quản lý tổ hợp môn",
                "Ngành - tổ hợp",
                "Điểm thí sinh",
                "Điểm cộng",
                "Nguyện vọng / Xét tuyển",
                "Bảng quy đổi"
        };

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppColor.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(new UserManagementPanel(), "USER");
        contentPanel.add(new CandidateManagementPanel(), "CANDIDATE");
        contentPanel.add(createPlaceholderPanel("Quản lý ngành"), "MAJOR");
        contentPanel.add(createPlaceholderPanel("Quản lý tổ hợp môn"), "COMBINATION");
        contentPanel.add(new MajorCombinationPanel(), "MAJOR_COMBINATION");
        contentPanel.add(createPlaceholderPanel("Điểm thí sinh"), "SCORE");
        contentPanel.add(createPlaceholderPanel("Điểm cộng"), "BONUS");
        contentPanel.add(createPlaceholderPanel("Nguyện vọng / Xét tuyển"), "ASPIRATION");
        contentPanel.add(new ScoreConversionPanel(), "CONVERSION");
        String[] cardKeys = {
                "USER", "CANDIDATE", "MAJOR", "COMBINATION",
                "MAJOR_COMBINATION", "SCORE", "BONUS", "ASPIRATION", "CONVERSION"
        };

        for (int i = 0; i < menuNames.length; i++) {
            RoundedButton btn = new RoundedButton(menuNames[i], AppColor.PRIMARY, AppColor.PRIMARY_DARK, 18);
            btn.setPreferredSize(new Dimension(200, 45));
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            final String key = cardKeys[i];
            btn.addActionListener(e -> cardLayout.show(contentPanel, key));
            sidebar.add(btn);
        }

        sidebarWrapper.add(lblMenu, BorderLayout.NORTH);
        sidebarWrapper.add(sidebar, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(sidebarWrapper, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppColor.BACKGROUND);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppColor.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel label = new JLabel(title + " - Đang phát triển");
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(AppColor.TEXT_PRIMARY);

        card.add(label);
        wrapper.add(card, BorderLayout.CENTER);

        return wrapper;
    }
}