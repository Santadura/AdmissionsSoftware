package com.tuyensinh.ui.bonus;

import com.tuyensinh.entity.BonusScore;
import com.tuyensinh.service.BonusScoreService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class BonusManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private BonusScoreService service;

    public BonusManagementPanel() {
        this.service = new BonusScoreService();
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý điểm cộng 2025");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchBar.setBackground(AppColor.SURFACE);
        searchBar.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(220, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        RoundedButton btnSearch = new RoundedButton("Tìm kiếm", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        RoundedButton btnRefresh = new RoundedButton("Làm mới", new Color(100, 150, 200), new Color(70, 120, 170));

        searchBar.add(new JLabel("Tìm kiếm (CCCD):"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnRefresh);

        topPanel.add(searchBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLE ---
        String[] cols = {"ID", "CCCD", "Mã ngành", "Mã tổ hợp", "Phương thức", "Điểm CC", "Điểm HSG", "Tổng", "Ghi chú", "Keys"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(227, 242, 253));
        table.setGridColor(AppColor.BORDER);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));

        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColor.BORDER),
            new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scroll, BorderLayout.CENTER);
        add(centerCard, BorderLayout.CENTER);

        // --- BOTTOM ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        RoundedButton btnAdd = new RoundedButton("Thêm", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        RoundedButton btnImport = new RoundedButton("Import HSG", new Color(67, 160, 71), new Color(46, 125, 50));
        RoundedButton btnImportCC = new RoundedButton("Import CC", new Color(33, 150, 243), new Color(25, 118, 210));
        RoundedButton btnEdit = new RoundedButton("Sửa", new Color(251, 140, 0), new Color(239, 108, 0));
        RoundedButton btnDelete = new RoundedButton("Xóa", new Color(229, 57, 53), new Color(198, 40, 40));

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnImport);
        bottomPanel.add(btnImportCC);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- EVENTS ---
        btnSearch.addActionListener(e -> loadData());
        txtSearch.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(); });
        
        btnAdd.addActionListener(e -> addScore());
        btnImport.addActionListener(e -> importHsg());
        btnImportCC.addActionListener(e -> importEnglish());
        btnEdit.addActionListener(e -> editScore());
        btnDelete.addActionListener(e -> deleteScore());
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) editScore();
            }
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String searchTerm = txtSearch.getText().trim();
        
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                return service.getBonusScores(searchTerm);
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> data = get();
                    for (Object[] row : data) {
                        BonusScore b = (BonusScore) row[0];
                        String manganh = (String) row[1];
                        tableModel.addRow(new Object[]{
                            b.getId(),
                            b.getCccd(),
                            manganh,
                            b.getMaToHop(),
                            b.getPhuongThuc(),
                            b.getDiemCc(),
                            b.getDiemUtxt(),
                            b.getDiemTong(),
                            b.getGhiChu(),
                            b.getDcKeys()
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(BonusManagementPanel.this, "Lỗi tải dữ liệu: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void addScore() {
        BonusFormDialog dialog = new BonusFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                service.addBonusScore(dialog.getBonusScore());
                loadData();
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }

    private void importHsg() {
        BonusImportDialog dialog = new BonusImportDialog((JFrame) SwingUtilities.getWindowAncestor(this), service);
        dialog.setVisible(true);
        if (dialog.isImported()) {
            loadData();
        }
    }

    private void importEnglish() {
        EnglishBonusImportDialog dialog = new EnglishBonusImportDialog((JFrame) SwingUtilities.getWindowAncestor(this), service);
        dialog.setVisible(true);
        if (dialog.isImported()) {
            loadData();
        }
    }

    private void editScore() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi!");
            return;
        }
        
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        BonusScore score = service.getById(id);
        
        if (score != null) {
            BonusFormDialog dialog = new BonusFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), score);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                try {
                    service.updateBonusScore(dialog.getBonusScore());
                    loadData();
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
                }
            }
        }
    }

    private void deleteScore() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi!");
            return;
        }
        
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        int res = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản ghi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try {
                service.deleteBonusScore(id);
                loadData();
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }
}
