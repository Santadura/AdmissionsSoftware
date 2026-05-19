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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.tuyensinh.entity.XtNganh;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

public class MajorFormDialog extends JDialog {

    private JTextField txtManganh, txtTennganh, txtTohopgoc, txtChitieu, txtDiemsan;
    private JCheckBox chkThpt, chkDgnl, chkVsat;
    private boolean confirmed = false;
    private final XtNganh nganh;

    public MajorFormDialog(JFrame parent, XtNganh nganh) {
        super(parent, nganh == null ? "Thêm ngành mới" : "Sửa ngành", true);
        this.nganh = nganh == null ? new XtNganh() : nganh;
        initUI();
        if (nganh != null) fillData();
        setSize(480, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(AppColor.SURFACE);
        main.setBorder(new EmptyBorder(20, 25, 15, 25));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppColor.SURFACE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 5, 6, 5);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        String[] labels = {"Mã ngành *:", "Tên ngành *:", "Tổ hợp gốc:", "Chỉ tiêu:", "Điểm sàn:"};
        txtManganh  = new JTextField(); txtTennganh = new JTextField();
        txtTohopgoc = new JTextField(); txtChitieu  = new JTextField();
        txtDiemsan  = new JTextField();
        JTextField[] fields = {txtManganh, txtTennganh, txtTohopgoc, txtChitieu, txtDiemsan};

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]); lbl.setFont(labelFont);
            form.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.7;
            fields[i].setFont(fieldFont);
            fields[i].setPreferredSize(new Dimension(220, 30));
            form.add(fields[i], gc);
        }

        gc.gridx = 0; gc.gridy = labels.length; gc.weightx = 0.3;
        JLabel lblPt = new JLabel("Phương thức:"); lblPt.setFont(labelFont);
        form.add(lblPt, gc);

        gc.gridx = 1;
        JPanel ptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ptPanel.setBackground(AppColor.SURFACE);
        chkThpt = new JCheckBox("THPT"); chkDgnl = new JCheckBox("ĐGNL"); chkVsat = new JCheckBox("VSAT");
        for (JCheckBox chk : new JCheckBox[]{chkThpt, chkDgnl, chkVsat}) {
            chk.setFont(fieldFont); chk.setBackground(AppColor.SURFACE);
            ptPanel.add(chk);
        }
        form.add(ptPanel, gc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(AppColor.SURFACE);
        RoundedButton btnSave   = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(150,150,150), new Color(120,120,120));
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void fillData() {
        txtManganh.setText(nganh.getManganh()    != null ? nganh.getManganh()    : "");
        txtManganh.setEditable(false);
        txtTennganh.setText(nganh.getTennganh()  != null ? nganh.getTennganh()   : "");
        txtTohopgoc.setText(nganh.getNTohopgoc() != null ? nganh.getNTohopgoc()  : "");
        txtChitieu.setText(nganh.getNChitieu()   != null ? String.valueOf(nganh.getNChitieu()) : "");
        txtDiemsan.setText(nganh.getNDiemsan()   != null ? nganh.getNDiemsan().toString() : "");
        chkThpt.setSelected("1".equals(nganh.getNThpt()));
        chkDgnl.setSelected("1".equals(nganh.getNDgnl()));
        chkVsat.setSelected("1".equals(nganh.getNVsat()));
    }

    private void save() {
        String ma  = txtManganh.getText().trim();
        String ten = txtTennganh.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã ngành và tên ngành không được rỗng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        nganh.setManganh(ma);
        nganh.setTennganh(ten);
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
        nganh.setNThpt(chkThpt.isSelected() ? "1" : "0");
        nganh.setNDgnl(chkDgnl.isSelected() ? "1" : "0");
        nganh.setNVsat(chkVsat.isSelected() ? "1" : "0");
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public XtNganh getNganh()    { return nganh; }
}
