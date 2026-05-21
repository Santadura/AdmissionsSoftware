package com.tuyensinh.ui.conversion;

import com.tuyensinh.entity.ScoreConversion;
import com.tuyensinh.service.ScoreConversionService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ScoreConversionDialog extends JDialog {
    private JComboBox<String> cboPhuongThuc;
    private JTextField txtToHop, txtMon, txtMaQuyDoi, txtPhanVi;
    private JTextField txtDiemA, txtDiemB, txtDiemC, txtDiemD;

    private ScoreConversion scoreConversion;
    private ScoreConversionService service;
    private boolean isSaved = false;

    public ScoreConversionDialog(JFrame parent, ScoreConversion sc, ScoreConversionService service) {
        super(parent, sc == null ? "Thêm Bảng quy đổi" : "Sửa Bảng quy đổi", true);
        this.scoreConversion = sc == null ? new ScoreConversion() : sc;
        this.service = service;
        initUI();
        loadDataToForm();
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
        
        addLabel(mainPanel, "Phương thức:", gbc, row++);
        cboPhuongThuc = new JComboBox<>(new String[]{"THPT", "DGNL", "VSAT"});
        cboPhuongThuc.setPreferredSize(new Dimension(0, 35));
        gbc.gridy = row++;
        mainPanel.add(cboPhuongThuc, gbc);

        addLabel(mainPanel, "Tổ hợp (nếu có):", gbc, row++);
        txtToHop = new JTextField();
        addTextField(mainPanel, txtToHop, gbc, row++);

        addLabel(mainPanel, "Môn (nếu có):", gbc, row++);
        txtMon = new JTextField();
        addTextField(mainPanel, txtMon, gbc, row++);

        addLabel(mainPanel, "Mã quy đổi:", gbc, row++);
        txtMaQuyDoi = new JTextField();
        addTextField(mainPanel, txtMaQuyDoi, gbc, row++);

        addLabel(mainPanel, "Phân vị:", gbc, row++);
        txtPhanVi = new JTextField();
        addTextField(mainPanel, txtPhanVi, gbc, row++);

        // Separator
        gbc.gridy = row++;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel lblSep = new JLabel("Thông số nội suy (A -> B quy đổi sang C -> D)");
        lblSep.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblSep.setForeground(AppColor.PRIMARY);
        mainPanel.add(lblSep, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        addLabel(mainPanel, "Mốc điểm thi đầu (A):", gbc, row++);
        txtDiemA = new JTextField("0.0");
        addTextField(mainPanel, txtDiemA, gbc, row++);

        addLabel(mainPanel, "Mốc điểm thi cuối (B):", gbc, row++);
        txtDiemB = new JTextField("0.0");
        addTextField(mainPanel, txtDiemB, gbc, row++);

        addLabel(mainPanel, "Mốc quy đổi đầu (C):", gbc, row++);
        txtDiemC = new JTextField("0.0");
        addTextField(mainPanel, txtDiemC, gbc, row++);

        addLabel(mainPanel, "Mốc quy đổi cuối (D):", gbc, row++);
        txtDiemD = new JTextField("0.0");
        addTextField(mainPanel, txtDiemD, gbc, row++);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(AppColor.BACKGROUND);
        
        RoundedButton btnSave = new RoundedButton("Lưu thông tin", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        RoundedButton btnCancel = new RoundedButton("Hủy bỏ", new Color(158, 158, 158), new Color(117, 117, 117));
        
        btnSave.addActionListener(e -> saveAction());
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

    private void loadDataToForm() {
        if (scoreConversion.getIdqd() != null) {
            cboPhuongThuc.setSelectedItem(scoreConversion.getPhuongThuc());
            txtToHop.setText(scoreConversion.getToHop());
            txtMon.setText(scoreConversion.getMon());
            txtMaQuyDoi.setText(scoreConversion.getMaQuyDoi());
            txtPhanVi.setText(scoreConversion.getPhanVi());
            txtDiemA.setText(scoreConversion.getDiemA() != null ? String.valueOf(scoreConversion.getDiemA()) : "0.0");
            txtDiemB.setText(scoreConversion.getDiemB() != null ? String.valueOf(scoreConversion.getDiemB()) : "0.0");
            txtDiemC.setText(scoreConversion.getDiemC() != null ? String.valueOf(scoreConversion.getDiemC()) : "0.0");
            txtDiemD.setText(scoreConversion.getDiemD() != null ? String.valueOf(scoreConversion.getDiemD()) : "0.0");
        }
    }

    private void saveAction() {
        try {
            scoreConversion.setPhuongThuc(cboPhuongThuc.getSelectedItem().toString());
            scoreConversion.setToHop(txtToHop.getText().trim());
            scoreConversion.setMon(txtMon.getText().trim());
            scoreConversion.setMaQuyDoi(txtMaQuyDoi.getText().trim());
            scoreConversion.setPhanVi(txtPhanVi.getText().trim());
            scoreConversion.setDiemA(Double.parseDouble(txtDiemA.getText().trim()));
            scoreConversion.setDiemB(Double.parseDouble(txtDiemB.getText().trim()));
            scoreConversion.setDiemC(Double.parseDouble(txtDiemC.getText().trim()));
            scoreConversion.setDiemD(Double.parseDouble(txtDiemD.getText().trim()));

            service.save(scoreConversion);
            isSaved = true;
            JOptionPane.showMessageDialog(this, "Lưu thông tin thành công!");
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập điểm là số hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean showDialog() {
        setVisible(true);
        return isSaved;
    }
}
