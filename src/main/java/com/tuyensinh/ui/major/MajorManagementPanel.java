package com.tuyensinh.ui.major;

import com.tuyensinh.entity.XtNganh;
import com.tuyensinh.service.NganhService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MajorManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private NganhService service;
    private List<XtNganh> currentList;

    public MajorManagementPanel() {
        this.service = new NganhService();
        initUI();
        loadData("");
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý ngành tuyển sinh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchBar.setBackground(AppColor.SURFACE);
        searchBar.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(220, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        RoundedButton btnSearch = new RoundedButton("Tìm kiếm", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        RoundedButton btnRefresh = new RoundedButton("Làm mới", new Color(100,150,200), new Color(70,120,170));

        searchBar.add(new JLabel("Tìm kiếm (mã/tên ngành):"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnRefresh);
        topPanel.add(searchBar, BorderLayout.SOUTH);

        // --- TABLE ---
        String[] cols = {"ID", "Mã ngành", "Tên ngành", "Tổ hợp gốc", "Chỉ tiêu", "Điểm sàn", "Điểm chuẩn", "THPT", "ĐGNL", "VSAT"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(227, 242, 253));
        table.setGridColor(AppColor.BORDER);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 

        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColor.BORDER),
            new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scroll, BorderLayout.CENTER);

        // --- BOTTOM BUTTONS ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        RoundedButton btnAdd    = new RoundedButton("Thêm",        AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        RoundedButton btnEdit   = new RoundedButton("Sửa",         new Color(251,140,0), new Color(239,108,0));
        RoundedButton btnDelete = new RoundedButton("Xóa",         new Color(229,57,53), new Color(198,40,40));
        RoundedButton btnImport = new RoundedButton("Import Excel", new Color(67,160,71), new Color(46,125,50));

        bottomPanel.add(btnImport);
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);

        add(topPanel,    BorderLayout.NORTH);
        add(centerCard,  BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setColumnWidths();

        // --- EVENTS ---
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(""); });
        btnAdd.addActionListener(e -> addNganh());
        btnEdit.addActionListener(e -> editNganh());
        btnDelete.addActionListener(e -> deleteNganh());
        btnImport.addActionListener(e -> importExcel());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) editNganh();
            }
        });
    }

    private void setColumnWidths() {
        int[] widths = { 50, 100, 250, 100, 80, 80, 100, 60, 60, 60 }; 
        for (int i = 0; i < widths.length; i++) {
            if (i < table.getColumnCount()) {
                TableColumn column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(widths[i]);
            }
        }
    }

    private void loadData(String kw) {
        tableModel.setRowCount(0);
        currentList = service.search(kw);
        for (XtNganh n : currentList) {
            tableModel.addRow(new Object[]{
                n.getIdnganh(), 
                n.getManganh(), 
                n.getTennganh(),
                n.getNTohopgoc(), 
                n.getNChitieu(),
                n.getNDiemsan() != null ? n.getNDiemsan() : "",
                n.getNDiemtrungtuyen() != null ? n.getNDiemtrungtuyen() : "Chưa xét", // Hiển thị "Chưa xét" nếu NULL
                "1".equals(n.getNThpt()) ? "✓" : "",
                "1".equals(n.getNDgnl()) ? "✓" : "",
                "1".equals(n.getNVsat()) ? "✓" : ""
            });
        }
    }

    private void addNganh() {
        MajorFormDialog dlg = new MajorFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            try {
                service.add(dlg.getNganh());
                loadData(txtSearch.getText().trim());
                JOptionPane.showMessageDialog(this, "Thêm ngành thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editNganh() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một ngành!"); return; }
        XtNganh selected = currentList.get(row);
        MajorFormDialog dlg = new MajorFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            try {
                service.update(dlg.getNganh());
                loadData(txtSearch.getText().trim());
                JOptionPane.showMessageDialog(this, "Cập nhật ngành thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteNganh() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một ngành!"); return; }
        XtNganh selected = currentList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xóa ngành '" + selected.getTennganh() + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.delete(selected.getIdnganh());
                loadData(txtSearch.getText().trim());
                JOptionPane.showMessageDialog(this, "Xóa ngành thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) { return f.getName().endsWith(".xlsx") || f.isDirectory(); }
            public String getDescription() { return "Excel files (*.xlsx)"; }
        });
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        SwingWorker<NganhService.ImportResult, Void> worker = new SwingWorker<>() {
            protected NganhService.ImportResult doInBackground() throws Exception {
                return service.importFromExcel(fc.getSelectedFile());
            }
            protected void done() {
                try {
                    NganhService.ImportResult r = get();
                    String msg = "Import thành công " + r.successCount + " ngành!";
                    if (!r.errors.isEmpty()) msg += "\n\nLỗi:\n" + String.join("\n", r.errors.subList(0, Math.min(5, r.errors.size())));
                    JOptionPane.showMessageDialog(MajorManagementPanel.this, msg);
                    loadData("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MajorManagementPanel.this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}