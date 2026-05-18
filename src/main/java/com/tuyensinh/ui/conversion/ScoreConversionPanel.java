package com.tuyensinh.ui.conversion;

import com.tuyensinh.entity.ScoreConversion;
import com.tuyensinh.service.ScoreConversionService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ScoreConversionPanel extends JPanel {

    private JTable tableConversion;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnSearch, btnRefresh, btnImport, btnAdd, btnEdit, btnDelete;
    
    private ScoreConversionService service;
    private String currentSearchTerm = "";

    public ScoreConversionPanel() {
        this.service = new ScoreConversionService();
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        //TOP PANEL 

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý Bảng quy đổi điểm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setBackground(AppColor.SURFACE);
        toolBar.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));

        // Tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);
        
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnSearch = new RoundedButton("Tìm kiếm", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnRefresh = new RoundedButton("Làm mới", new Color(100, 150, 200), new Color(70, 120, 170));

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        // Các nút hành động
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setOpaque(false);
        
        btnImport = new RoundedButton("Import Excel", new Color(67, 160, 71), new Color(46, 125, 50));
        btnAdd = new RoundedButton("Thêm mới", new Color(33, 150, 243), new Color(25, 118, 210));
        btnEdit = new RoundedButton("Sửa", new Color(251, 140, 0), new Color(239, 108, 0));
        btnDelete = new RoundedButton("Xóa", new Color(229, 57, 53), new Color(198, 40, 40));
        
        actionPanel.add(btnImport);
        actionPanel.add(btnAdd); 
        actionPanel.add(btnEdit); 
        actionPanel.add(btnDelete);

        toolBar.add(actionPanel, BorderLayout.WEST);
        toolBar.add(searchPanel, BorderLayout.EAST);

        topPanel.add(toolBar, BorderLayout.SOUTH);

        // CENTER PANEL 
        String[] columns = {"ID", "Phương thức", "Tổ hợp", "Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Mã quy đổi", "Phân vị"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableConversion = new JTable(tableModel);
        tableConversion.setRowHeight(30);
        tableConversion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableConversion.setSelectionBackground(new Color(227, 242, 253));
        tableConversion.setGridColor(AppColor.BORDER);

        JTableHeader header = tableConversion.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        JScrollPane scrollPane = new JScrollPane(tableConversion);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));

        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        // Ráp nối các thành phần vào Panel chính 
        add(topPanel, BorderLayout.NORTH);
        add(centerCard, BorderLayout.CENTER);

        setupEvents();
    }

    private void setupEvents() {
        btnSearch.addActionListener(e -> {
            currentSearchTerm = txtSearch.getText().trim().toLowerCase();
            loadData();
        });

        txtSearch.addActionListener(e -> btnSearch.doClick());

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            currentSearchTerm = "";
            loadData();
        });

        btnImport.addActionListener(e -> importExcel());
        btnAdd.addActionListener(e -> addRecord());
        btnEdit.addActionListener(e -> editRecord());
        btnDelete.addActionListener(e -> deleteRecord());

        tableConversion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) editRecord();
            }
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);

        List<ScoreConversion> list = currentSearchTerm.isEmpty() ? 
            service.getAll() : 
            service.search(currentSearchTerm);

        for (ScoreConversion sc : list) {
            tableModel.addRow(new Object[]{
                sc.getIdqd(),
                sc.getPhuongThuc(),
                sc.getToHop(),
                sc.getMon(),
                sc.getDiemA(),
                sc.getDiemB(),
                sc.getDiemC(),
                sc.getDiemD(),
                sc.getMaQuyDoi(),
                sc.getPhanVi()
            });
        }
    }

    private void addRecord() {
        ScoreConversionDialog dialog = new ScoreConversionDialog((JFrame) SwingUtilities.getWindowAncestor(this), null, service);
        if (dialog.showDialog()) {
            loadData();
        }
    }

    private void editRecord() {
        int selectedRow = tableConversion.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
            return;
        }

        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        ScoreConversion sc = service.getAll().stream()
                                .filter(item -> item.getIdqd().equals(id))
                                .findFirst()
                                .orElse(null);

        if (sc != null) {
            ScoreConversionDialog dialog = new ScoreConversionDialog((JFrame) SwingUtilities.getWindowAncestor(this), sc, service);
            if (dialog.showDialog()) {
                loadData();
            }
        }
    }

    private void deleteRecord() {
        int selectedRow = tableConversion.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa!");
            return;
        }

        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        String maQuyDoi = (String) tableModel.getValueAt(selectedRow, 8);

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn xóa mã quy đổi: " + maQuyDoi + "?", 
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadData();
        }
    }

    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel (Bảng quy đổi điểm)");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "Đã chọn file: " + file.getName() + "\n(Chức năng đọc file Excel cần sử dụng thư viện Apache POI)");
            loadData();
        }
    }
}