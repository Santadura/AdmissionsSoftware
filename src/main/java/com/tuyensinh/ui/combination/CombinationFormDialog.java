package com.tuyensinh.ui.combination;

import com.tuyensinh.entity.XtToHopMon;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CombinationFormDialog extends JDialog {

    private JTextField txtMatohop, txtTentohop, txtMon1, txtMon2, txtMon3;
    private boolean confirmed = false;
    private final XtToHopMon tohop;

    public CombinationFormDialog(JFrame parent, XtToHopMon tohop) {
        super(parent, tohop == null ? "Thêm tổ hợp môn" : "Sửa tổ hợp môn", true);
        this.tohop = tohop == null ? new XtToHopMon() : tohop;
        initUI();
        if (tohop != null) fillData();
        setSize(420, 330);
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

        Font f = new Font("Segoe UI", Font.PLAIN, 13);
        String[] labels = {"Mã tổ hợp *:", "Tên tổ hợp:", "Môn 1 *:", "Môn 2 *:", "Môn 3 *:"};
        txtMatohop = new JTextField(); txtTentohop = new JTextField();
        txtMon1 = new JTextField(); txtMon2 = new JTextField(); txtMon3 = new JTextField();
        JTextField[] fields = {txtMatohop, txtTentohop, txtMon1, txtMon2, txtMon3};

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.35;
            JLabel lbl = new JLabel(labels[i]); lbl.setFont(f);
            form.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.65;
            fields[i].setFont(f);
            fields[i].setPreferredSize(new Dimension(200, 30));
            form.add(fields[i], gc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(AppColor.SURFACE);
        RoundedButton btnSave = new RoundedButton("Lưu", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
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
        txtMatohop.setText(tohop.getMatohop() != null ? tohop.getMatohop() : "");
        txtMatohop.setEditable(false);
        txtTentohop.setText(tohop.getTentohop() != null ? tohop.getTentohop() : "");
        txtMon1.setText(tohop.getMon1() != null ? tohop.getMon1() : "");
        txtMon2.setText(tohop.getMon2() != null ? tohop.getMon2() : "");
        txtMon3.setText(tohop.getMon3() != null ? tohop.getMon3() : "");
    }

    private void save() {
        String ma = txtMatohop.getText().trim();
        String m1 = txtMon1.getText().trim();
        String m2 = txtMon2.getText().trim();
        String m3 = txtMon3.getText().trim();
        if (ma.isEmpty() || m1.isEmpty() || m2.isEmpty() || m3.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã tổ hợp và 3 môn không được rỗng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tohop.setMatohop(ma);
        tohop.setTentohop(txtTentohop.getText().trim().isEmpty() ? null : txtTentohop.getText().trim());
        tohop.setMon1(m1); tohop.setMon2(m2); tohop.setMon3(m3);
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public XtToHopMon getTohop() { return tohop; }
}
