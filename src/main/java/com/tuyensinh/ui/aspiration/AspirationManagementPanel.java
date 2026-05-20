package com.tuyensinh.ui.aspiration;

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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.tuyensinh.entity.Aspiration;
import com.tuyensinh.service.AspirationService;
import com.tuyensinh.service.AspirationService.AdmissionResult;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

public class AspirationManagementPanel extends JPanel {

    private final AspirationService service;
    private JTable tableAspirations;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public AspirationManagementPanel() {
        this.service = new AspirationService();
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý nguyện vọng và xét tuyển");
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

        searchPanel.add(new JLabel("CCCD / mã ngành / phương thức / kết quả:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {
                "ID", "CCCD", "Họ tên", "Nguyện vọng", "Mã ngành", "Tổ hợp",
                "Điểm THM", "Điểm cộng", "Điểm UT", "Điểm XT", "Kết quả", "Phương thức"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableAspirations = new JTable(tableModel);
        tableAspirations.setRowHeight(30);
        tableAspirations.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableAspirations.setSelectionBackground(new Color(227, 242, 253));
        tableAspirations.setGridColor(AppColor.BORDER);

        JTableHeader header = tableAspirations.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        JScrollPane scrollPane = new JScrollPane(tableAspirations);
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

        JButton btnRunAdmission = new RoundedButton("Chạy xét tuyển", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnReport = new RoundedButton("Báo cáo", new Color(100, 100, 200), new Color(70, 70, 170));
        JButton btnAdd = new RoundedButton("Thêm", new Color(67, 160, 71), new Color(46, 125, 50));
        JButton btnEdit = new RoundedButton("Sửa", new Color(251, 140, 0), new Color(239, 108, 0));
        JButton btnDelete = new RoundedButton("Xóa", new Color(211, 47, 47), new Color(183, 28, 28));

        btnRunAdmission.addActionListener(e -> runAdmission());
        btnReport.addActionListener(e -> showReportDialog());
        btnAdd.addActionListener(e -> openFormDialog(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        tableAspirations.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    editSelected();
                }
            }
        });

        actionPanel.add(btnRunAdmission);
        actionPanel.add(btnReport);
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Object[]> data = service.getAspirationsWithCandidate(txtSearch == null ? "" : txtSearch.getText());
        for (Object[] row : data) {
            Aspiration aspiration = (Aspiration) row[0];
            String hoTen;
            if (row[1] != null || row[2] != null) {
                hoTen = (row[1] == null ? "" : row[1].toString()) + " " + (row[2] == null ? "" : row[2].toString());
            } else {
                hoTen = aspiration.getHoTen() != null ? aspiration.getHoTen() : "";
            }
            tableModel.addRow(new Object[]{
                    aspiration.getId(),
                    aspiration.getCccd(),
                    hoTen.trim(),
                    aspiration.getThuTu(),
                    aspiration.getMaNganh(),
                    aspiration.getToHop(),
                    aspiration.getDiemThxt(),
                    aspiration.getDiemCong(), // Sử dụng DiemCong (cột diem_cong) thay vì DiemCc
                    aspiration.getDiemUtqd(),
                    aspiration.getDiemXetTuyen(),
                    aspiration.getKetQua(),
                    aspiration.getPhuongThuc()
            });
        }
    }

    private void runAdmission() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Chạy xét tuyển sẽ cập nhật điểm cộng, điểm xét tuyển và kết quả cho tất cả nguyện vọng. Tiếp tục?",
                "Xác nhận xét tuyển",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<AdmissionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AdmissionResult doInBackground() {
                return service.runAdmission();
            }

            @Override
            protected void done() {
                try {
                    AdmissionResult result = get();
                    loadData();
                    JOptionPane.showMessageDialog(
                            AspirationManagementPanel.this,
                                "Đã xét " + result.getTotal() + " nguyện vọng.\n"
                                    + "Trúng tuyển: " + result.getPassed() + "\n"
                                    + "Không trúng tuyển: " + result.getFailed() + "\n"
                                    + "Dưới sàn: " + result.getBelowFloor() + "\n"
                                    + "Chưa có điểm: " + result.getMissingScore() + "\n"
                                    + "Chưa cấu hình ngành: " + result.getMissingMajorConfig());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AspirationManagementPanel.this,
                            ex.getMessage(),
                            "Loi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showReportDialog() {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Báo cáo kết quả trúng tuyển",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Detailed successful candidates
        String[] cols1 = {"CCCD", "Họ tên", "Nguyện vọng", "Mã ngành", "Tổ hợp", "Điểm THM", "Điểm cộng", "Điểm UT", "Điểm XT", "Phương thức"};
        DefaultTableModel model1 = new DefaultTableModel(cols1, 0);
        JTable table1 = new JTable(model1);
        table1.setRowHeight(25);
        List<Object[]> data1 = service.getSuccessfulCandidatesReport();
        for (Object[] row : data1) {
            String hoTen;
            if (row[2] != null || row[3] != null) {
                hoTen = (row[2] == null ? "" : row[2].toString()) + " " + (row[3] == null ? "" : row[3].toString());
            } else {
                hoTen = row[11] != null ? row[11].toString() : "";
            }
            model1.addRow(new Object[]{
                    row[1],         // CCCD
                    hoTen.trim(),   // Họ tên
                    row[10],        // Nguyện vọng (thuTu)
                    row[0],         // Mã ngành
                    row[4],         // Tổ hợp
                    row[5],         // Điểm THM
                    row[7],         // Điểm cộng
                    row[6],         // Điểm UT
                    row[8],         // Điểm XT
                    row[9]          // Phương thức
            });
        }
        tabbedPane.addTab("Danh sách trúng tuyển chi tiết", new JScrollPane(table1));

        // Tab 2: Count by method and major
        String[] cols2 = {"Mã ngành", "Phương thức", "Số lượng trúng tuyển"};
        DefaultTableModel model2 = new DefaultTableModel(cols2, 0);
        JTable table2 = new JTable(model2);
        table2.setRowHeight(25);
        List<Object[]> data2 = service.getAdmissionCountByMethodReport();
        for (Object[] row : data2) {
            model2.addRow(row);
        }
        tabbedPane.addTab("Thống kê theo phương thức", new JScrollPane(table2));

        dialog.add(tabbedPane, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new RoundedButton("Đóng", Color.GRAY, Color.DARK_GRAY);
        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void editSelected() {
        Integer id = getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa.");
            return;
        }

        Aspiration aspiration = service.getById(id);
        if (aspiration == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nguyện vọng.");
            return;
        }
        openFormDialog(aspiration);
    }

    private void deleteSelected() {
        Integer id = getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot dong de xoa.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xóa nguyện vọng đã chọn?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deleteAspiration(id);
            loadData();
            JOptionPane.showMessageDialog(this, "Đã xóa nguyện vọng.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer getSelectedId() {
        int selectedRow = tableAspirations.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        return (Integer) tableModel.getValueAt(selectedRow, 0);
    }

    private void openFormDialog(Aspiration current) {
        boolean editMode = current != null;
        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            editMode ? "Sửa nguyện vọng" : "Thêm nguyện vọng",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(560, 470);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(10, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(AppColor.SURFACE);

        JTextField txtCccd = new JTextField(editMode ? current.getCccd() : "");
        JTextField txtMaNganh = new JTextField(editMode ? current.getMaNganh() : "");
        JTextField txtThuTu = new JTextField(editMode ? integerToText(current.getThuTu()) : "");
        JTextField txtDiemThxt = new JTextField(editMode ? decimalToText(current.getDiemThxt()) : "");
        JTextField txtDiemUt = new JTextField(editMode ? decimalToText(current.getDiemUtxt()) : "");
        JTextField txtDiemCc = new JTextField(editMode ? decimalToText(current.getDiemCc()) : "");
        JTextField txtPhuongThuc = new JTextField(editMode ? current.getPhuongThuc() : "");
        JTextField txtToHop = new JTextField(editMode ? current.getToHop() : "");
        JComboBox<String> cboKetQua = new JComboBox<>(
            new String[]{"chưa xét", "trúng tuyển", "không trúng tuyển", "dưới sàn", "chưa cấu hình", "yes"});
        cboKetQua.setEditable(true);
        cboKetQua.setSelectedItem(editMode && current.getKetQua() != null ? current.getKetQua() : "chưa xét");

        formPanel.add(new JLabel("CCCD:"));
        formPanel.add(txtCccd);
        formPanel.add(new JLabel("Mã ngành:"));
        formPanel.add(txtMaNganh);
        formPanel.add(new JLabel("Thứ tự NV:"));
        formPanel.add(txtThuTu);
        formPanel.add(new JLabel("Điểm THM:"));
        formPanel.add(txtDiemThxt);
        formPanel.add(new JLabel("Điểm ưu tiên (chưa quy đổi):"));
        formPanel.add(txtDiemUt);
        formPanel.add(new JLabel("Điểm cộng (không quy đổi):"));
        formPanel.add(txtDiemCc);
        formPanel.add(new JLabel("Phương thức:"));
        formPanel.add(txtPhuongThuc);
        formPanel.add(new JLabel("Tổ hợp:"));
        formPanel.add(txtToHop);
        formPanel.add(new JLabel("Kết quả:"));
        formPanel.add(cboKetQua);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Điểm XT = THM + UT_QuyĐổi + Cộng."));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnCancel = new RoundedButton("Hủy", Color.GRAY, Color.DARK_GRAY);

        btnSave.addActionListener(e -> {
            try {
                Aspiration aspiration = editMode ? current : new Aspiration();
                aspiration.setCccd(txtCccd.getText());
                aspiration.setMaNganh(txtMaNganh.getText());
                aspiration.setThuTu(parseInteger(txtThuTu.getText()));
                aspiration.setDiemThxt(parseDecimal(txtDiemThxt.getText()));
                aspiration.setDiemUtxt(parseDecimal(txtDiemUt.getText()));
                aspiration.setDiemCc(parseDecimal(txtDiemCc.getText()));
                aspiration.setPhuongThuc(txtPhuongThuc.getText());
                aspiration.setToHop(txtToHop.getText());
                aspiration.setKetQua(cboKetQua.getSelectedItem() == null
                        ? null
                        : cboKetQua.getSelectedItem().toString());

                if (editMode) {
                    service.updateAspiration(aspiration);
                } else {
                    service.addAspiration(aspiration);
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

    private Integer parseInteger(String value) {
        String cleaned = value == null ? "" : value.trim();
        return cleaned.isEmpty() ? null : Integer.valueOf(cleaned);
    }

    private String decimalToText(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private String integerToText(Integer value) {
        return value == null ? "" : value.toString();
    }
}
