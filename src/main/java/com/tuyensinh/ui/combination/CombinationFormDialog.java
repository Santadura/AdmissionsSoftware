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
    }

    private void initUI() {
        setSize(450, 500);
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
        
        addLabel(mainPanel, "Mã tổ hợp *:", gbc, row++);
        txtMatohop = new JTextField();
        addTextField(mainPanel, txtMatohop, gbc, row++);

        addLabel(mainPanel, "Tên tổ hợp:", gbc, row++);
        txtTentohop = new JTextField();
        addTextField(mainPanel, txtTentohop, gbc, row++);

        addLabel(mainPanel, "Môn 1 *:", gbc, row++);
        txtMon1 = new JTextField();
        addTextField(mainPanel, txtMon1, gbc, row++);

        addLabel(mainPanel, "Môn 2 *:", gbc, row++);
        txtMon2 = new JTextField();
        addTextField(mainPanel, txtMon2, gbc, row++);

        addLabel(mainPanel, "Môn 3 *:", gbc, row++);
        txtMon3 = new JTextField();
        addTextField(mainPanel, txtMon3, gbc, row++);

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
        txtMatohop.setText(tohop.getMatohop() != null ? tohop.getMatohop() : "");
        txtMatohop.setEditable(tohop.getMatohop() == null);
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
