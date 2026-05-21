package com.tuyensinh.ui.major_combination;

import com.tuyensinh.entity.MajorCombination;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MajorCombinationFormDialog extends JDialog {

    private final com.tuyensinh.service.NganhService nganhService = new com.tuyensinh.service.NganhService();
    private final com.tuyensinh.service.ToHopMonService toHopService = new com.tuyensinh.service.ToHopMonService();

    private JComboBox<com.tuyensinh.entity.XtNganh> cbNganh;
    private JComboBox<com.tuyensinh.entity.XtToHopMon> cbToHop;
    private JTextField tfMon1, tfHs1;
    private JTextField tfMon2, tfHs2;
    private JTextField tfMon3, tfHs3;
    private JTextField tfDoLech;
    
    private boolean confirmed = false;
    private MajorCombination majorCombination;

    public MajorCombinationFormDialog(JFrame parent, MajorCombination mc) {
        super(parent, mc == null ? "Thêm Ngành - Tổ hợp" : "Sửa Ngành - Tổ hợp", true);
        this.majorCombination = mc != null ? mc : new MajorCombination();
        initUI();
        loadInitialData();
        if (mc != null) {
            fillData();
        }
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
        
        addLabel(mainPanel, "Ngành tuyển sinh *:", gbc, row++);
        cbNganh = new JComboBox<>();
        cbNganh.setEditable(true);
        cbNganh.setPreferredSize(new Dimension(0, 35));
        cbNganh.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = row++;
        mainPanel.add(cbNganh, gbc);

        addLabel(mainPanel, "Mã tổ hợp xét tuyển *:", gbc, row++);
        cbToHop = new JComboBox<>();
        cbToHop.setEditable(true);
        cbToHop.setPreferredSize(new Dimension(0, 35));
        cbToHop.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbToHop.addActionListener(e -> autoFillSubjects());
        gbc.gridy = row++;
        mainPanel.add(cbToHop, gbc);

        // Mon 1
        JPanel pnlMon1 = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlMon1.setOpaque(false);
        JPanel p1 = new JPanel(new BorderLayout()); p1.setOpaque(false);
        p1.add(new JLabel("Môn 1:"), BorderLayout.NORTH);
        tfMon1 = new JTextField(); tfMon1.setPreferredSize(new Dimension(0, 35));
        p1.add(tfMon1, BorderLayout.CENTER);
        
        JPanel p2 = new JPanel(new BorderLayout()); p2.setOpaque(false);
        p2.add(new JLabel("Hệ số 1:"), BorderLayout.NORTH);
        tfHs1 = new JTextField("1"); tfHs1.setPreferredSize(new Dimension(0, 35));
        p2.add(tfHs1, BorderLayout.CENTER);
        
        pnlMon1.add(p1); pnlMon1.add(p2);
        gbc.gridy = row++;
        mainPanel.add(pnlMon1, gbc);

        // Mon 2
        JPanel pnlMon2 = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlMon2.setOpaque(false);
        JPanel p3 = new JPanel(new BorderLayout()); p3.setOpaque(false);
        p3.add(new JLabel("Môn 2:"), BorderLayout.NORTH);
        tfMon2 = new JTextField(); tfMon2.setPreferredSize(new Dimension(0, 35));
        p3.add(tfMon2, BorderLayout.CENTER);
        
        JPanel p4 = new JPanel(new BorderLayout()); p4.setOpaque(false);
        p4.add(new JLabel("Hệ số 2:"), BorderLayout.NORTH);
        tfHs2 = new JTextField("1"); tfHs2.setPreferredSize(new Dimension(0, 35));
        p4.add(tfHs2, BorderLayout.CENTER);
        
        pnlMon2.add(p3); pnlMon2.add(p4);
        gbc.gridy = row++;
        mainPanel.add(pnlMon2, gbc);

        // Mon 3
        JPanel pnlMon3 = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlMon3.setOpaque(false);
        JPanel p5 = new JPanel(new BorderLayout()); p5.setOpaque(false);
        p5.add(new JLabel("Môn 3:"), BorderLayout.NORTH);
        tfMon3 = new JTextField(); tfMon3.setPreferredSize(new Dimension(0, 35));
        p5.add(tfMon3, BorderLayout.CENTER);
        
        JPanel p6 = new JPanel(new BorderLayout()); p6.setOpaque(false);
        p6.add(new JLabel("Hệ số 3:"), BorderLayout.NORTH);
        tfHs3 = new JTextField("1"); tfHs3.setPreferredSize(new Dimension(0, 35));
        p6.add(tfHs3, BorderLayout.CENTER);
        
        pnlMon3.add(p5); pnlMon3.add(p6);
        gbc.gridy = row++;
        mainPanel.add(pnlMon3, gbc);

        addLabel(mainPanel, "Điểm lệch:", gbc, row++);
        tfDoLech = new JTextField("0.0");
        addTextField(mainPanel, tfDoLech, gbc, row++);

        // Buttons
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

    private void loadInitialData() {
        // Load Ngành
        java.util.List<com.tuyensinh.entity.XtNganh> nganhs = nganhService.getAll();
        for (com.tuyensinh.entity.XtNganh n : nganhs) {
            cbNganh.addItem(n);
        }

        // Load Tổ hợp
        java.util.List<com.tuyensinh.entity.XtToHopMon> toHops = toHopService.getAll();
        for (com.tuyensinh.entity.XtToHopMon th : toHops) {
            cbToHop.addItem(th);
        }
    }

    private void autoFillSubjects() {
        Object selected = cbToHop.getSelectedItem();
        if (selected == null) return;
        
        String maToHop;
        if (selected instanceof com.tuyensinh.entity.XtToHopMon) {
            maToHop = ((com.tuyensinh.entity.XtToHopMon) selected).getMatohop();
        } else {
            String str = selected.toString();
            // Nếu người dùng chọn từ danh sách nhưng nó bị parse thành chuỗi "A00 - Khối A", ta chỉ lấy "A00"
            maToHop = str.contains(" - ") ? str.split(" - ")[0].trim() : str.trim();
        }
        
        com.tuyensinh.entity.XtToHopMon th = null;
        for (int i = 0; i < cbToHop.getItemCount(); i++) {
            com.tuyensinh.entity.XtToHopMon item = cbToHop.getItemAt(i);
            if (item.getMatohop().equalsIgnoreCase(maToHop)) {
                th = item;
                break;
            }
        }
        
        if (th != null) {
            tfMon1.setText(th.getMon1());
            tfMon2.setText(th.getMon2());
            tfMon3.setText(th.getMon3());
        }
    }

    private void fillData() {
        // Set selected Ngành
        if (majorCombination.getNganhId() != null) {
            for (int i = 0; i < cbNganh.getItemCount(); i++) {
                if (cbNganh.getItemAt(i).getIdnganh().equals(majorCombination.getNganhId())) {
                    cbNganh.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Set selected Tổ hợp
        if (majorCombination.getMaToHop() != null) {
            for (int i = 0; i < cbToHop.getItemCount(); i++) {
                if (cbToHop.getItemAt(i).getMatohop().equals(majorCombination.getMaToHop())) {
                    cbToHop.setSelectedIndex(i);
                    break;
                }
            }
        }

        tfMon1.setText(majorCombination.getThMon1());
        tfHs1.setText(majorCombination.getHsMon1() != null ? String.valueOf(majorCombination.getHsMon1()) : "1");
        tfMon2.setText(majorCombination.getThMon2());
        tfHs2.setText(majorCombination.getHsMon2() != null ? String.valueOf(majorCombination.getHsMon2()) : "1");
        tfMon3.setText(majorCombination.getThMon3());
        tfHs3.setText(majorCombination.getHsMon3() != null ? String.valueOf(majorCombination.getHsMon3()) : "1");
        tfDoLech.setText(majorCombination.getDoLech() != null ? String.valueOf(majorCombination.getDoLech()) : "0.0");
    }

    private String extractMa(Object obj) {
        if (obj == null) return "";
        if (obj instanceof com.tuyensinh.entity.XtNganh) return ((com.tuyensinh.entity.XtNganh) obj).getManganh();
        if (obj instanceof com.tuyensinh.entity.XtToHopMon) return ((com.tuyensinh.entity.XtToHopMon) obj).getMatohop();
        
        String str = obj.toString();
        return str.contains(" - ") ? str.split(" - ")[0].trim() : str.trim();
    }

    private boolean validateForm() {
        Object nganhObj = cbNganh.getSelectedItem();
        Object toHopObj = cbToHop.getSelectedItem();

        String maNganh = extractMa(nganhObj);
        String maToHop = extractMa(toHopObj);

        if (maNganh.isEmpty()) {
            showError("Vui lòng chọn hoặc nhập mã ngành");
            return false;
        }
        if (maToHop.isEmpty()) {
            showError("Vui lòng chọn hoặc nhập mã tổ hợp");
            return false;
        }

        com.tuyensinh.entity.XtNganh n = nganhService.searchByMaNganh(maNganh);
        if (n == null) {
            showError("Lỗi: Mã ngành '" + maNganh + "' không tồn tại trong danh mục Ngành!");
            return false;
        }
            
        com.tuyensinh.repository.ToHopMonRepository thmRepo = new com.tuyensinh.repository.ToHopMonRepository();
        if (!thmRepo.existsByMatohop(maToHop)) {
            showError("Lỗi: Mã tổ hợp '" + maToHop + "' không tồn tại trong danh mục Tổ hợp môn!");
            return false;
        }

        try {
            Integer.parseInt(tfHs1.getText().trim());
            Integer.parseInt(tfHs2.getText().trim());
            Integer.parseInt(tfHs3.getText().trim());
        } catch (NumberFormatException e) {
            showError("Hệ số phải là số nguyên");
            return false;
        }
        try {
            Double.parseDouble(tfDoLech.getText().trim());
        } catch (NumberFormatException e) {
            showError("Điểm lệch phải là số");
            return false;
        }
        return true;
    }

    private void saveData() {
        String maNganh = extractMa(cbNganh.getSelectedItem());
        String maToHop = extractMa(cbToHop.getSelectedItem());

        com.tuyensinh.entity.XtNganh n = nganhService.searchByMaNganh(maNganh);
        
        majorCombination.setNganhId(n.getIdnganh());
        majorCombination.setMaToHop(maToHop);
        majorCombination.setThMon1(tfMon1.getText().trim());
        majorCombination.setHsMon1(Integer.parseInt(tfHs1.getText().trim()));
        majorCombination.setThMon2(tfMon2.getText().trim());
        majorCombination.setHsMon2(Integer.parseInt(tfHs2.getText().trim()));
        majorCombination.setThMon3(tfMon3.getText().trim());
        majorCombination.setHsMon3(Integer.parseInt(tfHs3.getText().trim()));
        majorCombination.setDoLech(Double.parseDouble(tfDoLech.getText().trim()));
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public MajorCombination getMajorCombination() {
        return majorCombination;
    }
}
