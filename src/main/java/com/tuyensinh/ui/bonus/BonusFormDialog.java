package com.tuyensinh.ui.bonus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.tuyensinh.entity.BonusScore;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;
import java.util.List;

public class BonusFormDialog extends JDialog {

    private final com.tuyensinh.service.CandidateService candidateService = new com.tuyensinh.service.CandidateService();
    private final com.tuyensinh.service.NganhService nganhService = new com.tuyensinh.service.NganhService();
    private final com.tuyensinh.service.MajorCombinationService majorCombService = new com.tuyensinh.service.MajorCombinationService();

    private JTextField tfCccd;
    private JTextField tfHoTen;
    private JComboBox<com.tuyensinh.entity.XtNganh> cbNganh;
    private JComboBox<String> cbToHop;
    private JComboBox<String> cbPhuongThuc;
    private JTextField tfDiemCC;
    private JTextField tfDiemUT;
    private JTextArea taGhiChu;
    
    private boolean confirmed = false;
    private BonusScore bonusScore;

    public BonusFormDialog(JFrame parent, BonusScore bonusScore) {
        super(parent, bonusScore == null ? "Thêm điểm cộng" : "Sửa điểm cộng", true);
        this.bonusScore = bonusScore != null ? bonusScore : new BonusScore();
        initUI();
        if (bonusScore != null) {
            fillData();
        }
    }

    private void initUI() {
        setSize(450, 600);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(AppColor.SURFACE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        int row = 0;
        
        addLabel(mainPanel, "CCCD:", gbc, row++);
        tfCccd = new JTextField(20);
        addTextField(mainPanel, tfCccd, gbc, row++);

        addLabel(mainPanel, "Họ tên:", gbc, row++);
        tfHoTen = new JTextField(20);
        tfHoTen.setEditable(false);
        tfHoTen.setBackground(new Color(245, 245, 245));
        addTextField(mainPanel, tfHoTen, gbc, row++);

        tfCccd.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private javax.swing.Timer timer = new javax.swing.Timer(300, e -> {
                String cccd = tfCccd.getText().trim();
                if (cccd.length() >= 3) {
                    com.tuyensinh.entity.Candidate c = candidateService.getByCccd(cccd);
                    if (c != null) {
                        tfHoTen.setText(c.getHoTen());
                    } else {
                        tfHoTen.setText("");
                    }
                } else {
                    tfHoTen.setText("");
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

        addLabel(mainPanel, "Ngành tuyển sinh:", gbc, row++);
        cbNganh = new JComboBox<>();
        cbNganh.setEditable(true);
        List<com.tuyensinh.entity.XtNganh> listNganh = nganhService.getAll();
        for (com.tuyensinh.entity.XtNganh n : listNganh) cbNganh.addItem(n);
        gbc.gridy = row++;
        mainPanel.add(cbNganh, gbc);

        addLabel(mainPanel, "Mã tổ hợp:", gbc, row++);
        cbToHop = new JComboBox<>();
        cbToHop.setEditable(true);
        gbc.gridy = row++;
        mainPanel.add(cbToHop, gbc);

        cbNganh.addActionListener(e -> {
            Object selected = cbNganh.getSelectedItem();
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
                for (com.tuyensinh.entity.MajorCombination mc : combs) cbToHop.addItem(mc.getMaToHop());
            }
        });

        addLabel(mainPanel, "Phương thức:", gbc, row++);
        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "VSAT", "DGNL"});
        cbPhuongThuc.setEditable(true);
        gbc.gridy = row++;
        mainPanel.add(cbPhuongThuc, gbc);

        addLabel(mainPanel, "Điểm chứng chỉ:", gbc, row++);
        JPanel ccPanel = new JPanel(new BorderLayout(5, 0));
        ccPanel.setOpaque(false);
        tfDiemCC = new JTextField("0", 20);
        tfDiemCC.setPreferredSize(new Dimension(0, 35));
        tfDiemCC.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ccPanel.add(tfDiemCC, BorderLayout.CENTER);
        
        RoundedButton btnSyncCC = new RoundedButton("Đồng bộ CC", new Color(103, 58, 183), new Color(81, 45, 168));
        btnSyncCC.setPreferredSize(new Dimension(110, 35));
        btnSyncCC.addActionListener(e -> syncCandidateCert());
        ccPanel.add(btnSyncCC, BorderLayout.EAST);
        gbc.gridy = row++;
        mainPanel.add(ccPanel, gbc);

        addLabel(mainPanel, "Điểm cộng HSG:", gbc, row++);
        tfDiemUT = new JTextField("0", 20);
        addTextField(mainPanel, tfDiemUT, gbc, row++);

        addLabel(mainPanel, "Ghi chú:", gbc, row++);
        taGhiChu = new JTextArea(4, 20);
        taGhiChu.setLineWrap(true);
        taGhiChu.setWrapStyleWord(true);
        taGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollGhiChu = new JScrollPane(taGhiChu);
        scrollGhiChu.setPreferredSize(new Dimension(0, 80));
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        mainPanel.add(scrollGhiChu, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(AppColor.BACKGROUND);
        RoundedButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(158, 158, 158), new Color(117, 117, 117));
        btnSave.addActionListener(e -> {
            if (validateForm()) {
                saveData();
                confirmed = true;
                dispose();
            }
        });
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

    private void syncCandidateCert() {
        String cccd = tfCccd.getText().trim();
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD trước.");
            return;
        }
        String phuongThuc = (String) cbPhuongThuc.getSelectedItem();
        if (phuongThuc == null || phuongThuc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phương thức xét tuyển.");
            return;
        }
        com.tuyensinh.repository.EnglishCertificateRepository certRepo = new com.tuyensinh.repository.EnglishCertificateRepository();
        com.tuyensinh.entity.EnglishCertificate cert = certRepo.findByCccd(cccd);
        if (cert == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chứng chỉ cho thí sinh này.");
            return;
        }
        com.tuyensinh.service.EnglishCertificateService englishService = new com.tuyensinh.service.EnglishCertificateService();
        int level = englishService.getLevel(cert.getLoaiCc(), cert.getDiemSo().doubleValue());
        BigDecimal bonus = englishService.getBonusPoints(level, phuongThuc);
        tfDiemCC.setText(bonus.toString());
        if (taGhiChu.getText().trim().isEmpty()) {
            taGhiChu.setText("Đồng bộ từ CC: " + cert.getLoaiCc() + " (Bậc " + level + ")");
        }
    }

    private void fillData() {
        tfCccd.setText(bonusScore.getCccd());
        if (bonusScore.getCccd() != null) {
            com.tuyensinh.entity.Candidate c = candidateService.getByCccd(bonusScore.getCccd());
            if (c != null) {
                tfHoTen.setText(c.getHoTen());
            }
        }
        if (bonusScore.getNganhId() != null) {
            com.tuyensinh.entity.XtNganh n = nganhService.getById(bonusScore.getNganhId());
            if (n != null) {
                for (int i = 0; i < cbNganh.getItemCount(); i++) {
                    if (cbNganh.getItemAt(i).getIdnganh().equals(n.getIdnganh())) {
                        cbNganh.setSelectedIndex(i);
                        break;
                    }
                }
                if (bonusScore.getMaToHop() != null) {
                    cbToHop.setSelectedItem(bonusScore.getMaToHop());
                }
            }
        }
        cbPhuongThuc.setSelectedItem(bonusScore.getPhuongThuc());
        tfDiemCC.setText(bonusScore.getDiemCc() != null ? bonusScore.getDiemCc().toString() : "0");
        tfDiemUT.setText(bonusScore.getDiemUtxt() != null ? bonusScore.getDiemUtxt().toString() : "0");
        taGhiChu.setText(bonusScore.getGhiChu());
    }

    private boolean validateForm() {
        if (tfCccd.getText().trim().isEmpty()) {
            showError("CCCD không được để trống");
            return false;
        }
        Object selectedNganh = cbNganh.getSelectedItem();
        if (selectedNganh == null) {
            showError("Vui lòng chọn ngành");
            return false;
        }
        Object selectedToHop = cbToHop.getSelectedItem();
        if (selectedToHop == null) {
            showError("Vui lòng chọn hoặc nhập tổ hợp");
            return false;
        }
        try {
            new BigDecimal(tfDiemCC.getText().trim());
            new BigDecimal(tfDiemUT.getText().trim());
        } catch (NumberFormatException e) {
            showError("Điểm phải là số");
            return false;
        }
        return true;
    }

    private void saveData() {
        bonusScore.setCccd(tfCccd.getText().trim());
        Object selectedNganh = cbNganh.getSelectedItem();
        com.tuyensinh.entity.XtNganh n = null;
        if (selectedNganh instanceof com.tuyensinh.entity.XtNganh) {
            n = (com.tuyensinh.entity.XtNganh) selectedNganh;
        } else {
            String maNganhInput = selectedNganh.toString().split(" - ")[0].trim();
            n = nganhService.searchByMaNganh(maNganhInput);
        }
        if (n != null) bonusScore.setNganhId(n.getIdnganh());
        Object selectedToHop = cbToHop.getSelectedItem();
        if (selectedToHop != null) bonusScore.setMaToHop(selectedToHop.toString().trim().toUpperCase());
        Object selectedPT = cbPhuongThuc.getSelectedItem();
        if (selectedPT != null) bonusScore.setPhuongThuc(selectedPT.toString());
        bonusScore.setDiemCc(new BigDecimal(tfDiemCC.getText().trim()));
        bonusScore.setDiemUtxt(new BigDecimal(tfDiemUT.getText().trim()));
        bonusScore.setGhiChu(taGhiChu.getText().trim());
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public BonusScore getBonusScore() {
        return bonusScore;
    }
}
