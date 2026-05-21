// src/main/java/com/tuyensinh/ui/score/ScoreManagementPanel.java
package com.tuyensinh.ui.score;

import com.tuyensinh.entity.CandidateScore;
import com.tuyensinh.service.CandidateScoreService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ScoreManagementPanel extends JPanel {

    private JTable tableScores;
    private DefaultTableModel tableModel;

    private JTextField txtSearch;
    private JComboBox<String> cboLoai;

    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnImport;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnStatistic;

    private CandidateScoreService service;

    public ScoreManagementPanel() {

        service = new CandidateScoreService();

        initUI();
        loadData();
    }

    private void initUI() {

        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= TOP =================

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý điểm thí sinh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);

        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(AppColor.SURFACE);
        searchPanel.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        cboLoai = new JComboBox<>(new String[]{
                "Tất cả",
                "THPT",
                "VSAT",
                "DGNL"
        });

        cboLoai.setPreferredSize(new Dimension(120, 35));

        btnSearch = new RoundedButton(
                "Tìm kiếm",
                AppColor.PRIMARY,
                AppColor.PRIMARY_DARK
        );

        btnRefresh = new RoundedButton(
                "Làm mới",
                new Color(100, 150, 200),
                new Color(70, 120, 170)
        );

        searchPanel.add(new JLabel("CCCD/SBD:"));
        searchPanel.add(txtSearch);

        searchPanel.add(new JLabel("Loại điểm:"));
        searchPanel.add(cboLoai);

        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        topPanel.add(searchPanel, BorderLayout.SOUTH);

        // ================= TABLE =================

        String[] columns = {
            "ID",
            "CCCD",
            "SBD",
            "Loại",
            "TO",
            "LI",
            "HO",
            "SI",
            "SU",
            "DI",
            "VA",
            "N1_THI",
            "N1_CC",
            "CNCN",
            "CNNN",
            "TI",
            "KTPL",
            "NL1",
            "NK1",
            "NK2",
            "NK3",
            "NK4"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableScores = new JTable(tableModel);

        tableScores.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableScores.setRowHeight(30);
        tableScores.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableScores.setGridColor(AppColor.BORDER);
        tableScores.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        tableScores.getColumnModel().getColumn(1).setPreferredWidth(130);  // CCCD
        tableScores.getColumnModel().getColumn(2).setPreferredWidth(120);  // SBD
        tableScores.getColumnModel().getColumn(3).setPreferredWidth(90);   // Loại

        for (int i = 4; i < tableScores.getColumnCount(); i++) {
            tableScores.getColumnModel().getColumn(i).setPreferredWidth(90);
        }

        JTableHeader header = tableScores.getTableHeader();

        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        JScrollPane scrollPane = new JScrollPane(
                tableScores,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        JPanel centerCard = new JPanel(new BorderLayout());

        centerCard.setBackground(AppColor.SURFACE);

        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        centerCard.add(scrollPane, BorderLayout.CENTER);

        // ================= BOTTOM =================

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        btnImport = new RoundedButton(
                "Import Excel",
                new Color(67, 160, 71),
                new Color(46, 125, 50)
        );

        btnAdd = new RoundedButton(
                "Thêm",
                new Color(0, 150, 136),
                new Color(0, 121, 107)
        );

        btnEdit = new RoundedButton(
                "Sửa",
                new Color(251, 140, 0),
                new Color(239, 108, 0)
        );

        btnDelete = new RoundedButton(
                "Xóa",
                new Color(229, 57, 53),
                new Color(198, 40, 40)
        );

        btnStatistic = new RoundedButton(
                "Thống kê",
                new Color(94, 53, 177),
                new Color(69, 39, 160)
        );

        bottomPanel.add(btnImport);
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnStatistic);

        add(topPanel, BorderLayout.NORTH);
        add(centerCard, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setupEvents();
    }

    private void setupEvents() {

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cboLoai.setSelectedIndex(0);
            loadData();
        });

        btnSearch.addActionListener(e -> loadData());

        btnImport.addActionListener(e -> importExcel());

        btnAdd.addActionListener(e -> addScore());

        btnEdit.addActionListener(e -> editScore());

        btnDelete.addActionListener(e -> deleteScore());

        btnStatistic.addActionListener(e -> showStatistic());

        tableScores.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editScore();
                }
            }
        });
    }

    private void loadData() {

        tableModel.setRowCount(0);

        List<CandidateScore> scores = service.getAllScores();

        String keyword = txtSearch.getText().trim().toLowerCase();

        String loaiText = cboLoai.getSelectedItem().toString();
        String loaiCode = service.toLoaiCode(loaiText);

        for (CandidateScore s : scores) {

            boolean matchKeyword =
                    keyword.isEmpty()
                            || (s.getCccd() != null && s.getCccd().toLowerCase().contains(keyword))
                            || (s.getSobaodanh() != null && s.getSobaodanh().toLowerCase().contains(keyword));

            boolean matchLoai =
                    loaiText.equals("Tất cả")
                            || loaiCode.equalsIgnoreCase(s.getDPhuongthuc());

            if (matchKeyword && matchLoai) {

                tableModel.addRow(new Object[]{
                        s.getIddiemthi(),
                        s.getCccd(),
                        s.getSobaodanh(),
                        service.toLoaiText(s.getDPhuongthuc()),
                        s.getTo(),
                        s.getLi(),
                        s.getHo(),
                        s.getSi(),
                        s.getSu(),
                        s.getDi(),
                        s.getVa(),
                        s.getN1Thi(),
                        s.getN1Cc(),
                        s.getCncn(),
                        s.getCnnn(),
                        s.getTi(),
                        s.getKtpl(),
                        s.getNl1(),
                        s.getNk1(),
                        s.getNk2(),
                        s.getNk3(),
                        s.getNk4()
                });
            }
        }
    }

    private void importExcel() {

        ScoreImportDialog dialog = new ScoreImportDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                service
        );

        if (dialog.showDialog()) {
            loadData();
        }
    }

    private void addScore() {

        CandidateScore score = new CandidateScore();

        ScoreEditDialog dialog = new ScoreEditDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                score,
                service,
                true
        );

        if (dialog.showDialog()) {
            loadData();
        }
    }

    private void editScore() {

        int selectedRow = tableScores.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn dữ liệu");
            return;
        }

        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);

        CandidateScore score = service.findById(id);

        ScoreEditDialog dialog = new ScoreEditDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                score,
                service,
                false
        );

        if (dialog.showDialog()) {
            loadData();
        }
    }

    private void deleteScore() {

        int selectedRow = tableScores.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn dữ liệu");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);

        try {

            service.delete(id);

            JOptionPane.showMessageDialog(this,
                    "Xóa thành công");

            loadData();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi: " + e.getMessage()
            );
        }
    }

    private void showStatistic() {

        ScoreStatisticDialog dialog = new ScoreStatisticDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                service
        );

        dialog.showDialog();
    }
}