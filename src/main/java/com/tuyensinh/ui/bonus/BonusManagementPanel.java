package com.tuyensinh.ui.bonus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.tuyensinh.entity.BonusScore;
import com.tuyensinh.service.BonusScoreService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

public class BonusManagementPanel extends JPanel {

    private final BonusScoreService service;
    private JTable tableBonus;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public BonusManagementPanel() {
        this.service = new BonusScoreService();
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quan ly diem cong");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(AppColor.SURFACE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)));

        txtSearch = new JTextField(24);
        txtSearch.setPreferredSize(new Dimension(260, 35));
        JButton btnSearch = new RoundedButton("Tìm kiếm", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnRefresh = new RoundedButton("Làm mới", new Color(100, 150, 200), new Color(70, 120, 170));

        btnSearch.addActionListener(e -> loadData());
        txtSearch.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        searchPanel.add(new JLabel("CCCD / ma nganh / to hop / phuong thuc:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {
                "ID", "CCCD", "Ma nganh", "To hop", "Phuong thuc",
                "Diem CC", "Diem UTXT", "Diem tong", "Ghi chu", "Key"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableBonus = new JTable(tableModel);
        tableBonus.setRowHeight(30);
        tableBonus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableBonus.setSelectionBackground(new Color(227, 242, 253));
        tableBonus.setGridColor(AppColor.BORDER);

        JTableHeader header = tableBonus.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        JScrollPane scrollPane = new JScrollPane(tableBonus);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));

        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scrollPane, BorderLayout.CENTER);
        add(centerCard, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = new RoundedButton("Thêm", new Color(67, 160, 71), new Color(46, 125, 50));
        JButton btnEdit = new RoundedButton("Sửa", new Color(251, 140, 0), new Color(239, 108, 0));
        JButton btnDelete = new RoundedButton("Xóa", new Color(211, 47, 47), new Color(183, 28, 28));

        btnAdd.addActionListener(e -> openFormDialog(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        tableBonus.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    editSelected();
                }
            }
        });

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<BonusScore> bonusScores = service.getBonusScores(txtSearch == null ? "" : txtSearch.getText());
        for (BonusScore bonusScore : bonusScores) {
            tableModel.addRow(new Object[]{
                    bonusScore.getId(),
                    bonusScore.getCccd(),
                    bonusScore.getMaNganh(),
                    bonusScore.getMaToHop(),
                    bonusScore.getPhuongThuc(),
                    bonusScore.getDiemCc(),
                    bonusScore.getDiemUtxt(),
                    bonusScore.getDiemTong(),
                    bonusScore.getGhiChu(),
                    bonusScore.getDcKeys()
            });
        }
    }

    private void editSelected() {
        Integer id = getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot dong de sua.");
            return;
        }

        BonusScore bonusScore = service.getById(id);
        if (bonusScore == null) {
            JOptionPane.showMessageDialog(this, "Khong tim thay diem cong.");
            return;
        }
        openFormDialog(bonusScore);
    }

    private void deleteSelected() {
        Integer id = getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot dong de xoa.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xoa diem cong da chon?",
                "Xac nhan",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deleteBonusScore(id);
            loadData();
            JOptionPane.showMessageDialog(this, "Da xoa diem cong.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer getSelectedId() {
        int selectedRow = tableBonus.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        return (Integer) tableModel.getValueAt(selectedRow, 0);
    }

    private void openFormDialog(BonusScore current) {
        boolean editMode = current != null;
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                editMode ? "Sua diem cong" : "Them diem cong",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(AppColor.SURFACE);

        JTextField txtCccd = new JTextField(editMode ? current.getCccd() : "");
        JTextField txtMaNganh = new JTextField(editMode ? current.getMaNganh() : "");
        JTextField txtMaToHop = new JTextField(editMode ? current.getMaToHop() : "");
        JTextField txtPhuongThuc = new JTextField(editMode ? current.getPhuongThuc() : "");
        JTextField txtDiemCc = new JTextField(editMode ? decimalToText(current.getDiemCc()) : "");
        JTextField txtDiemUtxt = new JTextField(editMode ? decimalToText(current.getDiemUtxt()) : "");
        JTextField txtDiemTong = new JTextField(editMode ? decimalToText(current.getDiemTong()) : "");
        JTextField txtGhiChu = new JTextField(editMode ? current.getGhiChu() : "");

        formPanel.add(new JLabel("CCCD:"));
        formPanel.add(txtCccd);
        formPanel.add(new JLabel("Ma nganh:"));
        formPanel.add(txtMaNganh);
        formPanel.add(new JLabel("Ma to hop:"));
        formPanel.add(txtMaToHop);
        formPanel.add(new JLabel("Phuong thuc:"));
        formPanel.add(txtPhuongThuc);
        formPanel.add(new JLabel("Diem cong chung:"));
        formPanel.add(txtDiemCc);
        formPanel.add(new JLabel("Diem UTXT:"));
        formPanel.add(txtDiemUtxt);
        formPanel.add(new JLabel("Diem tong:"));
        formPanel.add(txtDiemTong);
        formPanel.add(new JLabel("Ghi chu:"));
        formPanel.add(txtGhiChu);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Bo trong diem tong de tu cong CC + UTXT."));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnCancel = new RoundedButton("Hủy", Color.GRAY, Color.DARK_GRAY);

        btnSave.addActionListener(e -> {
            try {
                BonusScore bonusScore = editMode ? current : new BonusScore();
                bonusScore.setCccd(txtCccd.getText());
                bonusScore.setMaNganh(txtMaNganh.getText());
                bonusScore.setMaToHop(txtMaToHop.getText());
                bonusScore.setPhuongThuc(txtPhuongThuc.getText());
                bonusScore.setDiemCc(parseDecimal(txtDiemCc.getText()));
                bonusScore.setDiemUtxt(parseDecimal(txtDiemUtxt.getText()));
                bonusScore.setDiemTong(parseDecimal(txtDiemTong.getText()));
                bonusScore.setGhiChu(txtGhiChu.getText());

                if (editMode) {
                    service.updateBonusScore(bonusScore);
                } else {
                    service.addBonusScore(bonusScore);
                }

                loadData();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        actionPanel.add(btnSave);
        actionPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(actionPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private BigDecimal parseDecimal(String value) {
        String cleaned = value == null ? "" : value.trim();
        return cleaned.isEmpty() ? null : new BigDecimal(cleaned);
    }

    private String decimalToText(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
