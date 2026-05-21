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
    private final com.tuyensinh.service.NganhService nganhService = new com.tuyensinh.service.NganhService();
    private final com.tuyensinh.service.CandidateService candidateService = new com.tuyensinh.service.CandidateService();
    private final com.tuyensinh.service.MajorCombinationService majorCombService = new com.tuyensinh.service.MajorCombinationService();
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

        JLabel lblTitle = new JLabel("Quản lý nguyện vọng và xét tuyển 2025");
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

        searchPanel.add(new JLabel("Họ tên / CCCD / mã ngành / phương thức / kết quả:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {
                "ID", "CCCD", "Họ tên", "Nguyện vọng", "Mã Ngành", "Tổ hợp",
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
        JButton btnImport = new RoundedButton("Import Excel", new Color(33, 150, 243), new Color(25, 118, 210));
        JButton btnReport = new RoundedButton("Báo cáo", new Color(100, 100, 200), new Color(70, 70, 170));
        JButton btnAdd = new RoundedButton("Thêm", new Color(67, 160, 71), new Color(46, 125, 50));
        JButton btnEdit = new RoundedButton("Sửa", new Color(251, 140, 0), new Color(239, 108, 0));
        JButton btnDelete = new RoundedButton("Xóa", new Color(211, 47, 47), new Color(183, 28, 28));

        btnRunAdmission.addActionListener(e -> runAdmission());
        btnImport.addActionListener(e -> {
            AspirationImportDialog dialog = new AspirationImportDialog(
                (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this), service);
            dialog.setVisible(true);
            if (dialog.isImported()) {
                loadData();
            }
        });
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
        actionPanel.add(btnImport);
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
                hoTen = "";
            }
            tableModel.addRow(new Object[]{
                    aspiration.getId(),
                    aspiration.getCccd(),
                    hoTen.trim(),
                    aspiration.getThuTu(),
                    row[3] == null ? "" : row[3].toString(),
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

        // Create a loading dialog
        JDialog loadingDialog = new JDialog((javax.swing.JFrame) SwingUtilities.getWindowAncestor(this), "Đang xử lý", true);
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 30, 20, 30));
        p.add(new JLabel("Hệ thống đang chạy xét tuyển, vui lòng đợi trong giây lát..."), BorderLayout.CENTER);
        
        javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar();
        progressBar.setIndeterminate(true);
        p.add(progressBar, BorderLayout.SOUTH);
        
        loadingDialog.add(p);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(this);

        SwingWorker<AdmissionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AdmissionResult doInBackground() {
                return service.runAdmission();
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
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
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        loadingDialog.setVisible(true); // This will block until loadingDialog.dispose() is called
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
        String[] cols1 = {"CCCD", "Họ tên", "Nguyện vọng", "Mã Ngành", "Tổ hợp", "Điểm THM", "Điểm cộng", "Điểm UT", "Điểm XT", "Phương thức"};
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
                    row[0],         // ID Ngành
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
        String[] cols2 = {"Mã Ngành", "Phương thức", "Số lượng trúng tuyển"};
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
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(AppColor.SURFACE);

        JTextField txtCccd = new JTextField(editMode ? current.getCccd() : "");
        JTextField txtHoTen = new JTextField(editMode ? "" : "");
        txtHoTen.setEditable(false);
        txtHoTen.setBackground(new Color(245, 245, 245));

        if (editMode && current.getCccd() != null) {
            com.tuyensinh.entity.Candidate c = candidateService.getByCccd(current.getCccd());
            if (c != null) {
                txtHoTen.setText(c.getHoTen());
            }
        }

        txtCccd.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private javax.swing.Timer timer = new javax.swing.Timer(300, e -> {
                String cccd = txtCccd.getText().trim();
                if (cccd.length() >= 3) {
                    com.tuyensinh.entity.Candidate c = candidateService.getByCccd(cccd);
                    if (c != null) {
                        txtHoTen.setText(c.getHoTen());
                    } else {
                        txtHoTen.setText("");
                    }
                } else {
                    txtHoTen.setText("");
                }
            });

            public void insertUpdate(javax.swing.event.DocumentEvent e) { restartTimer(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { restartTimer(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { restartTimer(); }
            
            private void restartTimer() {
                timer.setRepeats(false);
                if (timer.isRunning()) timer.restart();
                else timer.start();
            }
        });

        JTextField txtThuTu = new JTextField(editMode ? integerToText(current.getThuTu()) : "");

        JComboBox<com.tuyensinh.entity.XtNganh> cbMaNganh = new JComboBox<>();
        cbMaNganh.setEditable(true);
        List<com.tuyensinh.entity.XtNganh> listNganh = nganhService.getAll();
        for (com.tuyensinh.entity.XtNganh n : listNganh) cbMaNganh.addItem(n);

        JComboBox<String> cbToHop = new JComboBox<>();
        cbToHop.setEditable(true);

        cbMaNganh.addActionListener(e -> {
            Object selected = cbMaNganh.getSelectedItem();
            if (selected == null) return;
            
            com.tuyensinh.entity.XtNganh targetNganh = null;
            if (selected instanceof com.tuyensinh.entity.XtNganh) {
                targetNganh = (com.tuyensinh.entity.XtNganh) selected;
            } else {
                String input = selected.toString().split(" - ")[0].trim();
                targetNganh = nganhService.searchByMaNganh(input);
            }
            
            cbToHop.removeAllItems();
            if (targetNganh != null) {
                List<com.tuyensinh.entity.MajorCombination> combs = majorCombService.getByNganhId(targetNganh.getIdnganh());
                for (com.tuyensinh.entity.MajorCombination mc : combs) {
                    cbToHop.addItem(mc.getMaToHop());
                }
            }
        });

        if (editMode && current.getNganhId() != null) {
            com.tuyensinh.entity.XtNganh n = nganhService.getById(current.getNganhId());
            if (n != null) {
                for (int i = 0; i < cbMaNganh.getItemCount(); i++) {
                    if (cbMaNganh.getItemAt(i).getIdnganh().equals(n.getIdnganh())) {
                        cbMaNganh.setSelectedIndex(i);
                        break;
                    }
                }
                if (current.getToHop() != null) {
                    cbToHop.setSelectedItem(current.getToHop());
                }
            }
        }

        JTextField txtDiemThxt = new JTextField(editMode ? decimalToText(current.getDiemThxt()) : "");
        JTextField txtDiemCong = new JTextField(editMode ? decimalToText(current.getDiemCong()) : "");
        JTextField txtDiemUt = new JTextField(editMode ? decimalToText(current.getDiemUtqd()) : "");
        JTextField txtDiemXt = new JTextField(editMode ? decimalToText(current.getDiemXetTuyen()) : "");
        JComboBox<String> cboKetQua = new JComboBox<>(
            new String[]{"chuaxet", "trungtuyen", "khongtrungtuyen", "duoisan", "chuacauhinh", "yes"});
        cboKetQua.setEditable(true);
        cboKetQua.setSelectedItem(editMode && current.getKetQua() != null ? current.getKetQua() : "chuaxet");

        JComboBox<String> cboPhuongThuc = new JComboBox<>(new String[]{"THPT", "VSAT", "DGNL"});
        if (editMode && current.getPhuongThuc() != null) {
            cboPhuongThuc.setSelectedItem(current.getPhuongThuc());
        }

        formPanel.add(new JLabel("CCCD:"));
        formPanel.add(txtCccd);
        formPanel.add(new JLabel("Họ tên:"));
        formPanel.add(txtHoTen);
        formPanel.add(new JLabel("Nguyện vọng (Thứ tự):"));
        formPanel.add(txtThuTu);
        formPanel.add(new JLabel("Mã Ngành:"));
        formPanel.add(cbMaNganh);
        formPanel.add(new JLabel("Mã Tổ hợp:"));
        formPanel.add(cbToHop);
        formPanel.add(new JLabel("Điểm THM:"));
        formPanel.add(txtDiemThxt);
        formPanel.add(new JLabel("Điểm cộng:"));
        formPanel.add(txtDiemCong);
        formPanel.add(new JLabel("Điểm UT:"));
        formPanel.add(txtDiemUt);
        formPanel.add(new JLabel("Điểm XT:"));
        formPanel.add(txtDiemXt);
        formPanel.add(new JLabel("Kết quả:"));
        formPanel.add(cboKetQua);
        formPanel.add(new JLabel("Phương thức:"));
        formPanel.add(cboPhuongThuc);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnCancel = new RoundedButton("Hủy", Color.GRAY, Color.DARK_GRAY);

        btnSave.addActionListener(e -> {
            try {
                Aspiration aspiration = editMode ? current : new Aspiration();
                aspiration.setCccd(txtCccd.getText());

                aspiration.setThuTu(parseInteger(txtThuTu.getText()));
                
                Object selectedNganh = cbMaNganh.getSelectedItem();
                if (selectedNganh == null) throw new Exception("Vui lòng chọn ngành!");
                
                com.tuyensinh.entity.XtNganh n = null;
                if (selectedNganh instanceof com.tuyensinh.entity.XtNganh) {
                    n = (com.tuyensinh.entity.XtNganh) selectedNganh;
                } else {
                    String maNganhInput = selectedNganh.toString().split(" - ")[0].trim();
                    n = nganhService.searchByMaNganh(maNganhInput);
                }

                if (n == null) {
                    JOptionPane.showMessageDialog(dialog, "Mã ngành không tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                aspiration.setNganhId(n.getIdnganh());
                
                Object selectedToHop = cbToHop.getSelectedItem();
                if (selectedToHop == null) throw new Exception("Vui lòng chọn hoặc nhập tổ hợp!");
                aspiration.setToHop(selectedToHop.toString().trim().toUpperCase());
                
                aspiration.setDiemThxt(parseDecimal(txtDiemThxt.getText()));

                BigDecimal diemCong = parseDecimal(txtDiemCong.getText());
                aspiration.setDiemCong(diemCong);
                aspiration.setDiemCc(diemCong);

                BigDecimal diemUt = parseDecimal(txtDiemUt.getText());
                aspiration.setDiemUtqd(diemUt);
                if (!editMode) {
                    aspiration.setDiemUtxt(diemUt);
                }

                aspiration.setDiemXetTuyen(parseDecimal(txtDiemXt.getText()));
                aspiration.setPhuongThuc(cboPhuongThuc.getSelectedItem().toString());
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
