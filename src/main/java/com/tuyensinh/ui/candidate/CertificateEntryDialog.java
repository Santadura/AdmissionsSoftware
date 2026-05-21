package com.tuyensinh.ui.candidate;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.entity.EnglishCertificate;
import com.tuyensinh.repository.EnglishCertificateRepository;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class CertificateEntryDialog extends JDialog {
    private final Candidate candidate;
    private final EnglishCertificateRepository repository;
    private JComboBox<String> cboType;
    private JTextField txtScore;

    public CertificateEntryDialog(JFrame parent, Candidate candidate) {
        super(parent, "Thêm chứng chỉ cho: " + candidate.getHoTen(), true);
        this.candidate = candidate;
        this.repository = new EnglishCertificateRepository();

        initUI();
        setSize(400, 250);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(AppColor.BACKGROUND);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setOpaque(false);

        form.add(new JLabel("Loại chứng chỉ:"));
        cboType = new JComboBox<>(new String[]{
            "IELTS", "TOEFL ITP", "TOEFL IBT", "TOEIC (4 kỹ năng)", 
            "PTE Academic", "Linguaskill", "Aptis ESOL", "VSTEP"
        });
        form.add(cboType);

        form.add(new JLabel("Điểm số:"));
        txtScore = new JTextField();
        form.add(txtScore);

        main.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        JButton btnSave = new RoundedButton("Lưu chứng chỉ", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnSave.addActionListener(e -> save());
        footer.add(btnSave);
        main.add(footer, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void save() {
        try {
            EnglishCertificate ec = new EnglishCertificate();
            ec.setCccd(candidate.getCccd());
            ec.setLoaiCc(cboType.getSelectedItem().toString());
            ec.setDiemSo(new BigDecimal(txtScore.getText().trim()));
            
            repository.save(ec);
            JOptionPane.showMessageDialog(this, "Đã lưu chứng chỉ. Hệ thống sẽ tự động quy đổi khi chạy xét tuyển.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }
}
