package com.tuyensinh.ui.candidate;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.service.CandidateService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CandidateEditDialog extends JDialog {
    
    private Candidate candidate;
    private CandidateService service;
    private boolean saved = false;
    
    private JTextField txtCccd;
    private JTextField txtSobaodanh;
    private JTextField txtHo;
    private JTextField txtTen;
    private JTextField txtNgaySinh;
    private JComboBox<String> cboGioiTinh;
    private JTextField txtNoiSinh;
    private JTextField txtDienThoai;
    private JTextField txtEmail;
    private JTextField txtDoiTuong;
    private JTextField txtKhuVuc;
    
    public CandidateEditDialog(JFrame parent, Candidate candidate, CandidateService service) {
        super(parent, "Sửa thông tin thí sinh", true);
        this.candidate = candidate;
        this.service = service;
        
        initComponents();
        loadCandidateData();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(AppColor.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(AppColor.SURFACE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        int row = 0;
        
        addLabel(formPanel, gbc, row, "CCCD:");
        txtCccd = createTextField();
        addField(formPanel, gbc, row, txtCccd);
        row++;
        
        addLabel(formPanel, gbc, row, "Số báo danh:");
        txtSobaodanh = createTextField();
        addField(formPanel, gbc, row, txtSobaodanh);
        row++;
        
        addLabel(formPanel, gbc, row, "Họ:");
        txtHo = createTextField();
        addField(formPanel, gbc, row, txtHo);
        row++;
        
        addLabel(formPanel, gbc, row, "Tên:");
        txtTen = createTextField();
        addField(formPanel, gbc, row, txtTen);
        row++;
        
        addLabel(formPanel, gbc, row, "Ngày sinh:");
        txtNgaySinh = createTextField();
        addField(formPanel, gbc, row, txtNgaySinh);
        row++;
        
        addLabel(formPanel, gbc, row, "Giới tính:");
        cboGioiTinh = new JComboBox<>(new String[]{"", "Nam", "Nữ", "Khác"});
        cboGioiTinh.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addField(formPanel, gbc, row, cboGioiTinh);
        row++;
        
        addLabel(formPanel, gbc, row, "Nơi sinh:");
        txtNoiSinh = createTextField();
        addField(formPanel, gbc, row, txtNoiSinh);
        row++;
        
        addLabel(formPanel, gbc, row, "Điện thoại:");
        txtDienThoai = createTextField();
        addField(formPanel, gbc, row, txtDienThoai);
        row++;
        
        addLabel(formPanel, gbc, row, "Email:");
        txtEmail = createTextField();
        addField(formPanel, gbc, row, txtEmail);
        row++;
        
        addLabel(formPanel, gbc, row, "ĐT ưu tiên:");
        txtDoiTuong = createTextField();
        addField(formPanel, gbc, row, txtDoiTuong);
        row++;
        
        addLabel(formPanel, gbc, row, "KV ưu tiên:");
        txtKhuVuc = createTextField();
        addField(formPanel, gbc, row, txtKhuVuc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        RoundedButton btnSave = new RoundedButton("Lưu", new Color(67, 160, 71), new Color(46, 125, 50));
        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(150, 150, 150), new Color(100, 100, 100));
        
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void addLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lbl, gbc);
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, int row, JComponent field) {
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setPreferredSize(new Dimension(200, 30));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }
    
    private void loadCandidateData() {
        txtCccd.setText(candidate.getCccd() != null ? candidate.getCccd() : "");
        txtSobaodanh.setText(candidate.getSobaodanh() != null ? candidate.getSobaodanh() : "");
        txtHo.setText(candidate.getHo() != null ? candidate.getHo() : "");
        txtTen.setText(candidate.getTen() != null ? candidate.getTen() : "");
        txtNgaySinh.setText(candidate.getNgaySinh() != null ? candidate.getNgaySinh() : "");
        
        if (candidate.getGioiTinh() != null) {
            cboGioiTinh.setSelectedItem(candidate.getGioiTinh());
        }
        
        txtNoiSinh.setText(candidate.getNoiSinh() != null ? candidate.getNoiSinh() : "");
        txtDienThoai.setText(candidate.getDienThoai() != null ? candidate.getDienThoai() : "");
        txtEmail.setText(candidate.getEmail() != null ? candidate.getEmail() : "");
        txtDoiTuong.setText(candidate.getDoiTuong() != null ? candidate.getDoiTuong() : "");
        txtKhuVuc.setText(candidate.getKhuVuc() != null ? candidate.getKhuVuc() : "");
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
        candidate.setNgaySinh(txtNgaySinh.getText().trim());
        candidate.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        candidate.setNoiSinh(txtNoiSinh.getText().trim());
        candidate.setDienThoai(txtDienThoai.getText().trim());
        candidate.setEmail(txtEmail.getText().trim());
        candidate.setDoiTuong(txtDoiTuong.getText().trim());
        candidate.setKhuVuc(txtKhuVuc.getText().trim());
        
        try {
            service.updateCandidate(candidate);
            saved = true;
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
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