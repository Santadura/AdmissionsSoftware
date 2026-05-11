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

        JLabel lblTitle = new JLabel("Quan ly nguyen vong va xet tuyen");
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
        JButton btnSearch = new RoundedButton("Tim kiem", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnRefresh = new RoundedButton("Lam moi", new Color(100, 150, 200), new Color(70, 120, 170));

        btnSearch.addActionListener(e -> loadData());
        txtSearch.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        searchPanel.add(new JLabel("CCCD / ma nganh / phuong thuc / ket qua:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {
                "ID", "CCCD", "Ma nganh", "TT", "Diem THXT",
                "Diem UT", "Diem cong", "Diem XT", "Ket qua", "Phuong thuc", "To hop", "Key"
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

        JButton btnRunAdmission = new RoundedButton("Chay xet tuyen", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnAdd = new RoundedButton("Them", new Color(67, 160, 71), new Color(46, 125, 50));
        JButton btnEdit = new RoundedButton("Sua", new Color(251, 140, 0), new Color(239, 108, 0));
        JButton btnDelete = new RoundedButton("Xoa", new Color(211, 47, 47), new Color(183, 28, 28));

        btnRunAdmission.addActionListener(e -> runAdmission());
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
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Aspiration> aspirations = service.getAspirations(txtSearch == null ? "" : txtSearch.getText());
        for (Aspiration aspiration : aspirations) {
            tableModel.addRow(new Object[]{
                    aspiration.getId(),
                    aspiration.getCccd(),
                    aspiration.getMaNganh(),
                    aspiration.getThuTu(),
                    aspiration.getDiemThxt(),
                    aspiration.getDiemUtqd(),
                    aspiration.getDiemCong(),
                    aspiration.getDiemXetTuyen(),
                    aspiration.getKetQua(),
                    aspiration.getPhuongThuc(),
                    aspiration.getToHop(),
                    aspiration.getNvKeys()
            });
        }
    }

    private void runAdmission() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Chay xet tuyen se cap nhat diem cong, diem xet tuyen va ket qua cho tat ca nguyen vong. Tiep tuc?",
                "Xac nhan xet tuyen",
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
                            "Da xet " + result.getTotal() + " nguyen vong.\n"
                                    + "Trung tuyen: " + result.getPassed() + "\n"
                                    + "Khong trung tuyen: " + result.getFailed() + "\n"
                                    + "Duoi san: " + result.getBelowFloor() + "\n"
                                    + "Chua co diem: " + result.getMissingScore() + "\n"
                                    + "Chua cau hinh nganh: " + result.getMissingMajorConfig());
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

    private void editSelected() {
        Integer id = getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot dong de sua.");
            return;
        }

        Aspiration aspiration = service.getById(id);
        if (aspiration == null) {
            JOptionPane.showMessageDialog(this, "Khong tim thay nguyen vong.");
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
                "Xoa nguyen vong da chon?",
                "Xac nhan",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deleteAspiration(id);
            loadData();
            JOptionPane.showMessageDialog(this, "Da xoa nguyen vong.");
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
                editMode ? "Sua nguyen vong" : "Them nguyen vong",
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
        JTextField txtDiemUt = new JTextField(editMode ? decimalToText(current.getDiemUtqd()) : "");
        JTextField txtDiemCong = new JTextField(editMode ? decimalToText(current.getDiemCong()) : "");
        JTextField txtPhuongThuc = new JTextField(editMode ? current.getPhuongThuc() : "");
        JTextField txtToHop = new JTextField(editMode ? current.getToHop() : "");
        JComboBox<String> cboKetQua = new JComboBox<>(
                new String[]{"chuaxet", "trungtuyen", "khongtrungtuyen", "duoisan", "chuacauhinh", "yes"});
        cboKetQua.setEditable(true);
        cboKetQua.setSelectedItem(editMode && current.getKetQua() != null ? current.getKetQua() : "chuaxet");

        formPanel.add(new JLabel("CCCD:"));
        formPanel.add(txtCccd);
        formPanel.add(new JLabel("Ma nganh:"));
        formPanel.add(txtMaNganh);
        formPanel.add(new JLabel("Thu tu NV:"));
        formPanel.add(txtThuTu);
        formPanel.add(new JLabel("Diem THXT:"));
        formPanel.add(txtDiemThxt);
        formPanel.add(new JLabel("Diem uu tien:"));
        formPanel.add(txtDiemUt);
        formPanel.add(new JLabel("Diem cong:"));
        formPanel.add(txtDiemCong);
        formPanel.add(new JLabel("Phuong thuc:"));
        formPanel.add(txtPhuongThuc);
        formPanel.add(new JLabel("To hop:"));
        formPanel.add(txtToHop);
        formPanel.add(new JLabel("Ket qua:"));
        formPanel.add(cboKetQua);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Diem XT duoc tinh tu THXT + uu tien + diem cong."));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new RoundedButton("Luu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        JButton btnCancel = new RoundedButton("Huy", Color.GRAY, Color.DARK_GRAY);

        btnSave.addActionListener(e -> {
            try {
                Aspiration aspiration = editMode ? current : new Aspiration();
                aspiration.setCccd(txtCccd.getText());
                aspiration.setMaNganh(txtMaNganh.getText());
                aspiration.setThuTu(parseInteger(txtThuTu.getText()));
                aspiration.setDiemThxt(parseDecimal(txtDiemThxt.getText()));
                aspiration.setDiemUtqd(parseDecimal(txtDiemUt.getText()));
                aspiration.setDiemCong(parseDecimal(txtDiemCong.getText()));
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
