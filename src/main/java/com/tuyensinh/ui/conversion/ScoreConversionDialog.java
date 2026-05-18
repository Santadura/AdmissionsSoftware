package com.tuyensinh.ui.conversion;

import com.tuyensinh.entity.ScoreConversion;
import com.tuyensinh.service.ScoreConversionService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ScoreConversionDialog extends JDialog {
    private JTextField txtPhuongThuc, txtToHop, txtMon, txtMaQuyDoi, txtPhanVi;
    private JTextField txtDiemA, txtDiemB, txtDiemC, txtDiemD;
    private JButton btnSave, btnCancel;

    private ScoreConversion scoreConversion;
    private ScoreConversionService service;
    private boolean isSaved = false;

    public ScoreConversionDialog(JFrame parent, ScoreConversion sc, ScoreConversionService service) {
        super(parent, sc == null ? "Thêm Bảng quy đổi" : "Sửa Bảng quy đổi", true);
        this.scoreConversion = sc == null ? new ScoreConversion() : sc;
        this.service = service;

        initUI();
        loadDataToForm();

        setSize(500, 450);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(AppColor.BACKGROUND);
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        formPanel.setBackground(AppColor.SURFACE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER),
                new EmptyBorder(15, 15, 15, 15)
        ));

        formPanel.add(new JLabel("Phương thức:")); txtPhuongThuc = new JTextField(); formPanel.add(txtPhuongThuc);
        formPanel.add(new JLabel("Tổ hợp:")); txtToHop = new JTextField(); formPanel.add(txtToHop);
        formPanel.add(new JLabel("Môn:")); txtMon = new JTextField(); formPanel.add(txtMon);
        formPanel.add(new JLabel("Mã quy đổi:")); txtMaQuyDoi = new JTextField(); formPanel.add(txtMaQuyDoi);
        formPanel.add(new JLabel("Phân vị:")); txtPhanVi = new JTextField(); formPanel.add(txtPhanVi);
        
        formPanel.add(new JLabel("Điểm A:")); txtDiemA = new JTextField("0.0"); formPanel.add(txtDiemA);
        formPanel.add(new JLabel("Điểm B:")); txtDiemB = new JTextField("0.0"); formPanel.add(txtDiemB);
        formPanel.add(new JLabel("Điểm C:")); txtDiemC = new JTextField("0.0"); formPanel.add(txtDiemC);
        formPanel.add(new JLabel("Điểm D:")); txtDiemD = new JTextField("0.0"); formPanel.add(txtDiemD);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        btnSave = new RoundedButton("Lưu thông tin", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnCancel = new RoundedButton("Hủy bỏ", new Color(158, 158, 158), new Color(117, 117, 117));
        
        actionPanel.add(btnSave);
        actionPanel.add(btnCancel);

        root.add(formPanel, BorderLayout.CENTER);
        root.add(actionPanel, BorderLayout.SOUTH);
        setContentPane(root);

        btnSave.addActionListener(e -> saveAction());
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadDataToForm() {
        if (scoreConversion.getIdqd() != null) {
            txtPhuongThuc.setText(scoreConversion.getPhuongThuc());
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
            scoreConversion.setPhuongThuc(txtPhuongThuc.getText().trim());
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