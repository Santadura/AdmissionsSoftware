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
    private final com.tuyensinh.service.CandidateService candidateService = new com.tuyensinh.service.CandidateService();
    private CandidateScoreService service;

    private boolean saved = false;
    private boolean isAdd;

    private JTextField txtCccd;
    private JTextField txtHoTen;
    private JTextField txtSbd;
    private JComboBox<String> cboLoai;
    private JTextField txtToan, txtLy, txtHoa, txtSinh, txtVan, txtAnh, txtSu, txtDi, txtKtpl, txtNl1, txtN1cc, txtTi, txtCncn, txtCnnn, txtNk1, txtNk2, txtNk3, txtNk4;

    public ScoreEditDialog(JFrame parent, CandidateScore score, CandidateScoreService service, boolean isAdd) {
        super(parent, isAdd ? "Thêm điểm" : "Sửa điểm", true);
        this.score = score;
        this.service = service;
        this.isAdd = isAdd;
        initUI();
        if (!isAdd) loadData();
    }

    private void initUI() {
        setSize(550, 850);
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

        addLabel(mainPanel, "Họ tên:", gbc, row++);
        txtHoTen = new JTextField();
        txtHoTen.setEditable(false);
        txtHoTen.setBackground(new Color(245, 245, 245));
        addTextField(mainPanel, txtHoTen, gbc, row++);

        txtCccd.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private javax.swing.Timer timer = new javax.swing.Timer(300, e -> {
                String cccd = txtCccd.getText().trim();
                if (cccd.length() >= 3) {
                    com.tuyensinh.entity.Candidate c = candidateService.getByCccd(cccd);
                    if (c != null) {
                        txtHoTen.setText(c.getHoTen());
                        txtSbd.setText(c.getSobaodanh());
                    } else {
                        txtHoTen.setText("");
                        txtSbd.setText("");
                    }
                } else {
                    txtHoTen.setText("");
                    txtSbd.setText("");
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

        addLabel(mainPanel, "SBD:", gbc, row++);
        txtSbd = new JTextField();
        addTextField(mainPanel, txtSbd, gbc, row++);

        addLabel(mainPanel, "Loại điểm:", gbc, row++);
        cboLoai = new JComboBox<>(new String[]{"THPT", "VSAT", "DGNL"});
        cboLoai.setPreferredSize(new Dimension(0, 35));
        gbc.gridy = row++;
        mainPanel.add(cboLoai, gbc);

        // Subjects Row 1
        JPanel p1 = new JPanel(new GridLayout(1, 2, 10, 0)); p1.setOpaque(false);
        txtToan = createSubjectField(p1, "Toán:");
        txtLy = createSubjectField(p1, "Lý:");
        gbc.gridy = row++; mainPanel.add(p1, gbc);

        // Subjects Row 2
        JPanel p2 = new JPanel(new GridLayout(1, 2, 10, 0)); p2.setOpaque(false);
        txtHoa = createSubjectField(p2, "Hóa:");
        txtSinh = createSubjectField(p2, "Sinh:");
        gbc.gridy = row++; mainPanel.add(p2, gbc);

        // Subjects Row 3
        JPanel p3 = new JPanel(new GridLayout(1, 2, 10, 0)); p3.setOpaque(false);
        txtVan = createSubjectField(p3, "Văn:");
        txtAnh = createSubjectField(p3, "Anh (N1_Thi):");
        gbc.gridy = row++; mainPanel.add(p3, gbc);

        // Subjects Row 4
        JPanel p4 = new JPanel(new GridLayout(1, 2, 10, 0)); p4.setOpaque(false);
        txtSu = createSubjectField(p4, "Sử:");
        txtDi = createSubjectField(p4, "Địa:");
        gbc.gridy = row++; mainPanel.add(p4, gbc);

        // Subjects Row 5
        JPanel p5 = new JPanel(new GridLayout(1, 2, 10, 0)); p5.setOpaque(false);
        txtKtpl = createSubjectField(p5, "KTPL/GDCD:");
        txtNl1 = createSubjectField(p5, "NL1 (DGNL):");
        gbc.gridy = row++; mainPanel.add(p5, gbc);
        
        // Subjects Row 6
        JPanel p6 = new JPanel(new GridLayout(1, 2, 10, 0)); p6.setOpaque(false);
        txtN1cc = createSubjectField(p6, "N1_CC:");
        txtTi = createSubjectField(p6, "Tiếng Anh (TI):");
        gbc.gridy = row++; mainPanel.add(p6, gbc);

        // Subjects Row 7
        JPanel p7 = new JPanel(new GridLayout(1, 2, 10, 0)); p7.setOpaque(false);
        txtCncn = createSubjectField(p7, "CNCN:");
        txtCnnn = createSubjectField(p7, "CNNN:");
        gbc.gridy = row++; mainPanel.add(p7, gbc);

        // Subjects Row 8
        JPanel p8 = new JPanel(new GridLayout(1, 2, 10, 0)); p8.setOpaque(false);
        txtNk1 = createSubjectField(p8, "NK1:");
        txtNk2 = createSubjectField(p8, "NK2:");
        gbc.gridy = row++; mainPanel.add(p8, gbc);

        // Subjects Row 9
        JPanel p9 = new JPanel(new GridLayout(1, 2, 10, 0)); p9.setOpaque(false);
        txtNk3 = createSubjectField(p9, "NK3:");
        txtNk4 = createSubjectField(p9, "NK4:");
        gbc.gridy = row++; mainPanel.add(p9, gbc);

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

    private JTextField createSubjectField(JPanel parent, String label) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(lbl, BorderLayout.NORTH);
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 30));
        p.add(tf, BorderLayout.CENTER);
        parent.add(p);
        return tf;
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

    private void loadData() {
        txtCccd.setText(score.getCccd());
        if (score.getCccd() != null) {
            com.tuyensinh.entity.Candidate c = candidateService.getByCccd(score.getCccd());
            if (c != null) {
                txtHoTen.setText(c.getHoTen());
            }
        }
        txtSbd.setText(score.getSobaodanh());
        cboLoai.setSelectedItem(score.getDPhuongthuc());
        txtToan.setText(toString(score.getTo()));
        txtLy.setText(toString(score.getLi()));
        txtHoa.setText(toString(score.getHo()));
        txtSinh.setText(toString(score.getSi()));
        txtVan.setText(toString(score.getVa()));
        txtAnh.setText(toString(score.getN1Thi()));
        txtSu.setText(toString(score.getSu()));
        txtDi.setText(toString(score.getDi()));
        txtKtpl.setText(toString(score.getKtpl()));
        txtNl1.setText(toString(score.getNl1()));
        txtN1cc.setText(toString(score.getN1Cc()));
        txtTi.setText(toString(score.getTi()));
        txtCncn.setText(toString(score.getCncn()));
        txtCnnn.setText(toString(score.getCnnn()));
        txtNk1.setText(toString(score.getNk1()));
        txtNk2.setText(toString(score.getNk2()));
        txtNk3.setText(toString(score.getNk3()));
        txtNk4.setText(toString(score.getNk4()));
    }

    private String toString(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private BigDecimal decimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void save() {
        try {
            String cccdStr = txtCccd.getText().trim();
            if (cccdStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            com.tuyensinh.entity.Candidate c = candidateService.getByCccd(cccdStr);
            if (c == null) {
                JOptionPane.showMessageDialog(this, "CCCD thí sinh không tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            score.setCccd(cccdStr);
            score.setSobaodanh(txtSbd.getText().trim());
            score.setDPhuongthuc(cboLoai.getSelectedItem().toString());
            score.setTo(decimal(txtToan.getText()));
            score.setLi(decimal(txtLy.getText()));
            score.setHo(decimal(txtHoa.getText()));
            score.setSi(decimal(txtSinh.getText()));
            score.setVa(decimal(txtVan.getText()));
            score.setN1Thi(decimal(txtAnh.getText()));
            score.setSu(decimal(txtSu.getText()));
            score.setDi(decimal(txtDi.getText()));
            score.setKtpl(decimal(txtKtpl.getText()));
            score.setNl1(decimal(txtNl1.getText()));
            score.setN1Cc(decimal(txtN1cc.getText()));
            score.setTi(decimal(txtTi.getText()));
            score.setCncn(decimal(txtCncn.getText()));
            score.setCnnn(decimal(txtCnnn.getText()));
            score.setNk1(decimal(txtNk1.getText()));
            score.setNk2(decimal(txtNk2.getText()));
            score.setNk3(decimal(txtNk3.getText()));
            score.setNk4(decimal(txtNk4.getText()));

            if (isAdd) service.save(score);
            else service.update(score);

            saved = true;
            JOptionPane.showMessageDialog(this, "Lưu thành công");
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    public boolean showDialog() {
        setVisible(true);
        return saved;
    }
}
