// src/main/java/com/tuyensinh/ui/score/ScoreEditDialog.java
package com.tuyensinh.ui.score;

import com.tuyensinh.entity.CandidateScore;
import com.tuyensinh.service.CandidateScoreService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class ScoreEditDialog extends JDialog {

    private CandidateScore score;
    private CandidateScoreService service;

    private boolean saved = false;
    private boolean isAdd;

    private JTextField txtCccd;
    private JTextField txtSbd;

    private JComboBox<String> cboLoai;

    private JTextField txtToan;
    private JTextField txtLy;
    private JTextField txtHoa;
    private JTextField txtSinh;
    private JTextField txtVan;
    private JTextField txtAnh;

    public ScoreEditDialog(
            JFrame parent,
            CandidateScore score,
            CandidateScoreService service,
            boolean isAdd
    ) {

        super(parent,
                isAdd ? "Thêm điểm" : "Sửa điểm",
                true);

        this.score = score;
        this.service = service;
        this.isAdd = isAdd;

        initUI();

        if (!isAdd) {
            loadData();
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));

        txtCccd = new JTextField();
        txtSbd = new JTextField();

        cboLoai = new JComboBox<>(new String[]{
                "THPT",
                "VSAT",
                "DGNL"
        });

        txtToan = new JTextField();
        txtLy = new JTextField();
        txtHoa = new JTextField();
        txtSinh = new JTextField();
        txtVan = new JTextField();
        txtAnh = new JTextField();

        addField(form, "CCCD", txtCccd);
        addField(form, "SBD", txtSbd);
        addField(form, "Loại điểm", cboLoai);

        addField(form, "Toán", txtToan);
        addField(form, "Lý", txtLy);
        addField(form, "Hóa", txtHoa);
        addField(form, "Sinh", txtSinh);
        addField(form, "Văn", txtVan);
        addField(form, "Anh", txtAnh);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnSave = new RoundedButton(
                "Lưu",
                new Color(67, 160, 71),
                new Color(46, 125, 50)
        );

        JButton btnCancel = new RoundedButton(
                "Hủy",
                Color.GRAY,
                Color.DARK_GRAY
        );

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(form, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        btnSave.addActionListener(e -> save());

        btnCancel.addActionListener(e -> dispose());
    }

    private void addField(JPanel panel, String label, JComponent field) {

        JLabel lbl = new JLabel(label);

        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(lbl);
        panel.add(field);
    }

    private void loadData() {

        txtCccd.setText(score.getCccd());
        txtSbd.setText(score.getSobaodanh());

        cboLoai.setSelectedItem(score.getDPhuongthuc());

        txtToan.setText(toString(score.getTo()));
        txtLy.setText(toString(score.getLi()));
        txtHoa.setText(toString(score.getHo()));
        txtSinh.setText(toString(score.getSi()));
        txtVan.setText(toString(score.getVa()));
        txtAnh.setText(toString(score.getN1Thi()));
    }

    private String toString(BigDecimal value) {
        return value == null ? "" : value.toString();
    }

    private BigDecimal decimal(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return new BigDecimal(value);
    }

    private void save() {

        try {

            score.setCccd(txtCccd.getText().trim());
            score.setSobaodanh(txtSbd.getText().trim());

            score.setDPhuongthuc(
                    cboLoai.getSelectedItem().toString()
            );

            score.setTo(decimal(txtToan.getText()));
            score.setLi(decimal(txtLy.getText()));
            score.setHo(decimal(txtHoa.getText()));
            score.setSi(decimal(txtSinh.getText()));
            score.setVa(decimal(txtVan.getText()));
            score.setN1Thi(decimal(txtAnh.getText()));

            if (isAdd) {
                service.save(score);
            } else {
                service.update(score);
            }

            saved = true;

            JOptionPane.showMessageDialog(this,
                    "Lưu thành công");

            dispose();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi: " + e.getMessage()
            );
        }
    }

    public boolean showDialog() {

        setVisible(true);

        return saved;
    }
}