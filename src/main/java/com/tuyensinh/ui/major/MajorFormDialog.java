package com.tuyensinh.ui.major;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.tuyensinh.entity.XtNganh;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

public class MajorFormDialog extends JDialog {

    private JTextField txtManganh, txtTennganh, txtNamTS, txtTohopgoc, txtChitieu, txtDiemsan, txtDiemchuan;
    private JCheckBox chkThpt, chkDgnl, chkVsat;
    private boolean confirmed = false;
    private final XtNganh nganh;

    public MajorFormDialog(JFrame parent, XtNganh nganh) {
        super(parent, nganh == null ? "Thêm ngành mới" : "Sửa ngành", true);
        this.nganh = nganh == null ? new XtNganh() : nganh;
        initUI();
        if (nganh != null) fillData();
    }

    private void initUI() {
        setSize(480, 620);
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
        
        addLabel(mainPanel, "Mã ngành *:", gbc, row++);
        txtManganh = new JTextField();
        addTextField(mainPanel, txtManganh, gbc, row++);

        addLabel(mainPanel, "Tên ngành *:", gbc, row++);
        txtTennganh = new JTextField();
        addTextField(mainPanel, txtTennganh, gbc, row++);

        addLabel(mainPanel, "Năm tuyển sinh:", gbc, row++);
        txtNamTS = new JTextField("2025");
        txtNamTS.setEditable(false);
        txtNamTS.setBackground(new Color(245, 245, 245));
        addTextField(mainPanel, txtNamTS, gbc, row++);

        addLabel(mainPanel, "Tổ hợp gốc:", gbc, row++);
        txtTohopgoc = new JTextField();
        addTextField(mainPanel, txtTohopgoc, gbc, row++);

        addLabel(mainPanel, "Chỉ tiêu:", gbc, row++);
        txtChitieu = new JTextField();
        addTextField(mainPanel, txtChitieu, gbc, row++);

        addLabel(mainPanel, "Điểm sàn:", gbc, row++);
        txtDiemsan = new JTextField();
        addTextField(mainPanel, txtDiemsan, gbc, row++);

        addLabel(mainPanel, "Điểm chuẩn:", gbc, row++);
        txtDiemchuan = new JTextField();
        addTextField(mainPanel, txtDiemchuan, gbc, row++);

        addLabel(mainPanel, "Phương thức:", gbc, row++);
        JPanel ptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ptPanel.setBackground(AppColor.SURFACE);
        chkThpt = new JCheckBox("THPT"); 
        chkDgnl = new JCheckBox("ĐGNL"); 
        chkVsat = new JCheckBox("VSAT");
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        for (JCheckBox chk : new JCheckBox[]{chkThpt, chkDgnl, chkVsat}) {
            chk.setFont(fieldFont); 
            chk.setBackground(AppColor.SURFACE);
            ptPanel.add(chk);
        }
        gbc.gridy = row++;
        mainPanel.add(ptPanel, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(AppColor.BACKGROUND);
        
        RoundedButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
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

    private void fillData() {
        txtManganh.setText(nganh.getManganh() != null ? nganh.getManganh() : "");
        txtManganh.setEditable(nganh.getIdnganh() == null);
        txtTennganh.setText(nganh.getTennganh() != null ? nganh.getTennganh() : "");
        txtNamTS.setText("2025");
        txtTohopgoc.setText(nganh.getNTohopgoc() != null ? nganh.getNTohopgoc() : "");
        txtChitieu.setText(nganh.getNChitieu() != null ? String.valueOf(nganh.getNChitieu()) : "");
        txtDiemsan.setText(nganh.getNDiemsan() != null ? nganh.getNDiemsan().toString() : "");
        txtDiemchuan.setText(nganh.getNDiemtrungtuyen() != null ? nganh.getNDiemtrungtuyen().toString() : "");
        chkThpt.setSelected("1".equals(nganh.getNThpt()));
        chkDgnl.setSelected("1".equals(nganh.getNDgnl()));
        chkVsat.setSelected("1".equals(nganh.getNVsat()));
    }

    private void save() {
        String ma = txtManganh.getText().trim();
        String ten = txtTennganh.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã ngành và tên ngành không được rỗng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        nganh.setManganh(ma);
        nganh.setTennganh(ten);
        
        nganh.setNamTuyenSinh(2025);

        nganh.setNTohopgoc(txtTohopgoc.getText().trim().isEmpty() ? null : txtTohopgoc.getText().trim());
        try {
            String ct = txtChitieu.getText().trim();
            nganh.setNChitieu(ct.isEmpty() ? 0 : Integer.parseInt(ct));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Chỉ tiêu phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String ds = txtDiemsan.getText().trim();
            nganh.setNDiemsan(ds.isEmpty() ? null : new BigDecimal(ds));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Điểm sàn không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String dc = txtDiemchuan.getText().trim();
            nganh.setNDiemtrungtuyen(dc.isEmpty() ? null : new BigDecimal(dc));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Điểm chuẩn không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        nganh.setNThpt(chkThpt.isSelected() ? "1" : "0");
        nganh.setNDgnl(chkDgnl.isSelected() ? "1" : "0");
        nganh.setNVsat(chkVsat.isSelected() ? "1" : "0");
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public XtNganh getNganh()    { return nganh; }
}
