package com.tuyensinh.ui.candidate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.service.CandidateService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

public class CandidateEditDialog extends JDialog {
    
    private Candidate candidate;
    private CandidateService service;
    private boolean saved = false;
    
    private JTextField txtCccd, txtSobaodanh, txtHo, txtTen, txtNgaySinh, txtNoiSinh, txtDienThoai, txtEmail, txtNamTS;
    private JComboBox<String> cboGioiTinh, cboDoiTuong, cboKhuVuc;
    
    public CandidateEditDialog(JFrame parent, Candidate candidate, CandidateService service) {
        super(parent, candidate.getIdthisinh() == null ? "Thêm thí sinh mới" : "Sửa thông tin thí sinh", true);
        this.candidate = candidate;
        this.service = service;
        initUI();
        loadCandidateData();
    }
    
    private void initUI() {
        setSize(500, 650);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(AppColor.SURFACE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        int row = 0;
        
        addLabel(mainPanel, "CCCD *:", gbc, row++);
        txtCccd = new JTextField();
        addTextField(mainPanel, txtCccd, gbc, row++);

        txtCccd.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private javax.swing.Timer timer = new javax.swing.Timer(300, e -> {
                if (candidate.getIdthisinh() == null) {
                    String cccd = txtCccd.getText().trim();
                    if (cccd.length() == 12 || cccd.length() == 9) {
                        Candidate existing = service.getByCccd(cccd);
                        if (existing != null) {
                            SwingUtilities.invokeLater(() -> {
                                int option = JOptionPane.showConfirmDialog(
                                    CandidateEditDialog.this,
                                    "Thí sinh với CCCD này đã tồn tại. Bạn có muốn điền thông tin hiện có?",
                                    "Thông báo",
                                    JOptionPane.YES_NO_OPTION);
                                if (option == JOptionPane.YES_OPTION) {
                                    candidate = existing;
                                    loadCandidateData();
                                }
                            });
                        }
                    }
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

        addLabel(mainPanel, "Số báo danh:", gbc, row++);
        txtSobaodanh = new JTextField();
        addTextField(mainPanel, txtSobaodanh, gbc, row++);

        addLabel(mainPanel, "Họ:", gbc, row++);
        txtHo = new JTextField();
        addTextField(mainPanel, txtHo, gbc, row++);

        addLabel(mainPanel, "Tên:", gbc, row++);
        txtTen = new JTextField();
        addTextField(mainPanel, txtTen, gbc, row++);

        addLabel(mainPanel, "Ngày sinh:", gbc, row++);
        txtNgaySinh = new JTextField();
        addTextField(mainPanel, txtNgaySinh, gbc, row++);

        addLabel(mainPanel, "Giới tính:", gbc, row++);
        cboGioiTinh = new JComboBox<>(new String[]{"", "Nam", "Nữ", "Khác"});
        cboGioiTinh.setPreferredSize(new Dimension(0, 35));
        cboGioiTinh.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = row++;
        mainPanel.add(cboGioiTinh, gbc);

        addLabel(mainPanel, "Nơi sinh:", gbc, row++);
        txtNoiSinh = new JTextField();
        addTextField(mainPanel, txtNoiSinh, gbc, row++);

        addLabel(mainPanel, "Điện thoại:", gbc, row++);
        txtDienThoai = new JTextField();
        addTextField(mainPanel, txtDienThoai, gbc, row++);

        addLabel(mainPanel, "Email:", gbc, row++);
        txtEmail = new JTextField();
        addTextField(mainPanel, txtEmail, gbc, row++);

        addLabel(mainPanel, "ĐT ưu tiên:", gbc, row++);
        cboDoiTuong = new JComboBox<>(new String[]{"", "01", "02", "03", "04", "05", "06", "07"});
        cboDoiTuong.setEditable(true);
        cboDoiTuong.setPreferredSize(new Dimension(0, 35));
        cboDoiTuong.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = row++;
        mainPanel.add(cboDoiTuong, gbc);

        addLabel(mainPanel, "KV ưu tiên:", gbc, row++);
        cboKhuVuc = new JComboBox<>(new String[]{"", "1", "2NT", "2", "3"});
        cboKhuVuc.setEditable(true);
        cboKhuVuc.setPreferredSize(new Dimension(0, 35));
        cboKhuVuc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = row++;
        mainPanel.add(cboKhuVuc, gbc);

        addLabel(mainPanel, "Năm tuyển sinh:", gbc, row++);
        txtNamTS = new JTextField("2025");
        txtNamTS.setEditable(false);
        txtNamTS.setBackground(new Color(245, 245, 245));
        addTextField(mainPanel, txtNamTS, gbc, row++);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(AppColor.BACKGROUND);
        
        RoundedButton btnSave = new RoundedButton("Lưu", new Color(67, 160, 71), new Color(46, 125, 50));
        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(158, 158, 158), new Color(117, 117, 117));
        
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    private void addLabel(JPanel panel, String text, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label, gbc);
    }

    private void addTextField(JPanel panel, JTextField textField, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        textField.setPreferredSize(new Dimension(0, 35));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(textField, gbc);
    }
    
    private void loadCandidateData() {
        txtCccd.setText(candidate.getCccd() != null ? candidate.getCccd() : "");
        txtSobaodanh.setText(candidate.getSobaodanh() != null ? candidate.getSobaodanh() : "");
        txtHo.setText(candidate.getHo() != null ? candidate.getHo() : "");
        txtTen.setText(candidate.getTen() != null ? candidate.getTen() : "");
        txtNgaySinh.setText(candidate.getNgaySinh() != null ? candidate.getNgaySinh().toString() : "");
        if (candidate.getGioiTinh() != null) cboGioiTinh.setSelectedItem(candidate.getGioiTinh());
        txtNoiSinh.setText(candidate.getNoiSinh() != null ? candidate.getNoiSinh() : "");
        txtDienThoai.setText(candidate.getDienThoai() != null ? candidate.getDienThoai() : "");
        txtEmail.setText(candidate.getEmail() != null ? candidate.getEmail() : "");
        
        if (candidate.getDoiTuong() != null) {
            cboDoiTuong.setSelectedItem(candidate.getDoiTuong());
        } else {
            cboDoiTuong.setSelectedIndex(0);
        }
        
        if (candidate.getKhuVuc() != null) {
            cboKhuVuc.setSelectedItem(candidate.getKhuVuc());
        } else {
            cboKhuVuc.setSelectedIndex(0);
        }
        
        txtNamTS.setText(candidate.getNamTuyenSinh() != null ? candidate.getNamTuyenSinh().toString() : "");
    }
    
    private void save() {
        if (txtCccd.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "CCCD không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (txtHo.getText().trim().isEmpty() && txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và Tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        candidate.setCccd(txtCccd.getText().trim());
        candidate.setSobaodanh(txtSobaodanh.getText().trim());
        candidate.setHo(txtHo.getText().trim());
        candidate.setTen(txtTen.getText().trim());
        String dateStr = txtNgaySinh.getText().trim();
        if (!dateStr.isEmpty()) {
            try {
                candidate.setNgaySinh(java.time.LocalDate.parse(dateStr));
            } catch (Exception ex) {
                try {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    candidate.setNgaySinh(java.time.LocalDate.parse(dateStr, formatter));
                } catch(Exception ex2) {
                    JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng (yyyy-MM-dd hoặc dd/MM/yyyy)", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } else {
            candidate.setNgaySinh(null);
        }
        candidate.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        candidate.setNoiSinh(txtNoiSinh.getText().trim());
        candidate.setDienThoai(txtDienThoai.getText().trim());
        candidate.setEmail(txtEmail.getText().trim());
        
        Object selectedDt = cboDoiTuong.getSelectedItem();
        candidate.setDoiTuong(selectedDt != null ? selectedDt.toString().trim() : "");
        
        Object selectedKv = cboKhuVuc.getSelectedItem();
        candidate.setKhuVuc(selectedKv != null ? selectedKv.toString().trim() : "");
        
        try {
            String nam = txtNamTS.getText().trim();
            candidate.setNamTuyenSinh(nam.isEmpty() ? 2025 : Integer.parseInt(nam));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Năm tuyển sinh phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (candidate.getIdthisinh() == null) {
                service.saveCandidate(candidate);
                JOptionPane.showMessageDialog(this, "Thêm thí sinh thành công!");
            } else {
                service.updateCandidate(candidate);
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            }
            saved = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean showDialog() {
        setVisible(true);
        return saved;
    }
}
