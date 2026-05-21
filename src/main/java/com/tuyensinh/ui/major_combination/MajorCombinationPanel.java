package com.tuyensinh.ui.major_combination;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.tuyensinh.entity.MajorCombination;
import com.tuyensinh.service.MajorCombinationService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

public class MajorCombinationPanel extends JPanel {
    
    private JTable tableCombinations;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnSearch, btnRefresh, btnAdd, btnEdit, btnDelete, btnImport;
    
    private MajorCombinationService service;
    private String currentSearchTerm = "";
    
    public MajorCombinationPanel() {
        this.service = new MajorCombinationService();
        initUI();
        loadData();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // TOP PANEL 
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Danh sách Ngành - Tổ hợp");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(AppColor.SURFACE);
        searchPanel.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        btnSearch = new RoundedButton("Tìm kiếm", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnRefresh = new RoundedButton("Làm mới", new Color(100, 150, 200), new Color(70, 120, 170));
        
        searchPanel.add(new JLabel("Tìm kiếm (Mã ngành/Tổ hợp):"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        topPanel.add(searchPanel, BorderLayout.SOUTH);
        
        //  CENTER PANEL (Table)
        String[] columns = {
            "ID", "Mã ngành", "Mã tổ hợp", "Môn 1", "HS 1", "Môn 2", "HS 2", "Môn 3", "HS 3", "Điểm lệch", "TB Keys"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tableCombinations = new JTable(tableModel);
        tableCombinations.setRowHeight(30);
        tableCombinations.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableCombinations.setSelectionBackground(new Color(227, 242, 253));
        tableCombinations.setGridColor(AppColor.BORDER);
        
        setColumnWidths();
        
        JTableHeader header = tableCombinations.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        JScrollPane scrollPane = new JScrollPane(tableCombinations);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scrollPane, BorderLayout.CENTER);
        
        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        btnAdd = new RoundedButton("Thêm mới", new Color(0, 150, 136), new Color(0, 121, 107));
        btnEdit = new RoundedButton("Sửa thông tin", new Color(251, 140, 0), new Color(239, 108, 0));
        btnDelete = new RoundedButton("Xóa", new Color(229, 57, 53), new Color(198, 40, 40));
        btnImport = new RoundedButton("Import Excel", new Color(67, 160, 71), new Color(46, 125, 50));

        bottomPanel.add(btnImport);
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerCard, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setupEvents();
    }
    
    private void setColumnWidths() {
        int[] widths = { 50, 100, 100, 80, 50, 80, 50, 80, 50, 80, 150 };
        for (int i = 0; i < widths.length; i++) {
            if (i < tableCombinations.getColumnCount()) {
                TableColumn column = tableCombinations.getColumnModel().getColumn(i);
                column.setPreferredWidth(widths[i]);
            }
        }
    }

    private void setupEvents() {
        btnSearch.addActionListener(e -> {
            currentSearchTerm = txtSearch.getText().trim();
            loadData();
        });

        txtSearch.addActionListener(e -> btnSearch.doClick());

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            currentSearchTerm = "";
            loadData();
        });

        btnAdd.addActionListener(e -> addCombination());
        btnEdit.addActionListener(e -> editCombination());
        btnDelete.addActionListener(e -> deleteCombination());
        btnImport.addActionListener(e -> importExcel());

        tableCombinations.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) editCombination();
            }
        });
    }

    private void addCombination() {
        MajorCombinationFormDialog dialog = new MajorCombinationFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                service.save(dialog.getMajorCombination());
                loadData();
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, com.tuyensinh.ui.ErrorHandler.getFriendlyMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCombination() {
        int row = tableCombinations.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi!");
            return;
        }

        Integer id = (Integer) tableModel.getValueAt(row, 0);
        MajorCombination mc = service.getById(id);
        
        if (mc != null) {
            MajorCombinationFormDialog dialog = new MajorCombinationFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), mc);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                try {
                    service.save(dialog.getMajorCombination());
                    loadData();
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, com.tuyensinh.ui.ErrorHandler.getFriendlyMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteCombination() {
        int row = tableCombinations.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi!");
            return;
        }

        Integer id = (Integer) tableModel.getValueAt(row, 0);
        int res = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản ghi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try {
                service.delete(id);
                loadData();
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, com.tuyensinh.ui.ErrorHandler.getFriendlyMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file Excel tổ hợp - ngành");
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(".xlsx") || f.isDirectory(); }
            public String getDescription() { return "Excel files (*.xlsx)"; }
        });

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            SwingWorker<MajorCombinationService.ImportResult, Void> worker = new SwingWorker<>() {
                @Override
                protected MajorCombinationService.ImportResult doInBackground() throws Exception {
                    return service.importFromExcel(fc.getSelectedFile());
                }

                @Override
                protected void done() {
                    try {
                        MajorCombinationService.ImportResult r = get();
                        String msg = "Import thành công " + r.successCount + " bản ghi!";
                        if (!r.errors.isEmpty()) {
                            msg += "\n\nCảnh báo (" + r.errors.size() + " lỗi):\n" +
                                    String.join("\n", r.errors.subList(0, Math.min(5, r.errors.size())));
                        }
                        JOptionPane.showMessageDialog(MajorCombinationPanel.this, msg);
                        loadData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MajorCombinationPanel.this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
    
    private void loadData() {
        tableModel.setRowCount(0);

        List<Object[]> data = currentSearchTerm.isEmpty() ? 
            service.getAllWithMajor() : 
            service.search(currentSearchTerm);
            
        for (Object[] row : data) {
            MajorCombination mc = (MajorCombination) row[0];
            String manganh = (String) row[1];
            tableModel.addRow(new Object[]{
                mc.getId(),
                manganh,
                mc.getMaToHop(),
                mc.getThMon1(),
                mc.getHsMon1(),
                mc.getThMon2(),
                mc.getHsMon2(),
                mc.getThMon3(),
                mc.getHsMon3(),
                mc.getDoLech(),
                mc.getTbKeys()
            });
        }
    }
}
