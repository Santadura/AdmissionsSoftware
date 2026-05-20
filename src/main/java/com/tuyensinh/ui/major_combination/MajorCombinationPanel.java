package com.tuyensinh.ui.major_combination;

import com.tuyensinh.entity.MajorCombination;
import com.tuyensinh.service.MajorCombinationService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MajorCombinationPanel extends JPanel {
    
    private JTable tableCombinations;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnSearch, btnRefresh, btnImport; 
    
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
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Danh sách Ngành - Tổ hợp");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        
        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setOpaque(false);

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

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setOpaque(false);
        
        btnImport = new RoundedButton("Import Excel", new Color(67, 160, 71), new Color(46, 125, 50));
        actionPanel.add(btnImport);

        toolBar.add(searchPanel, BorderLayout.EAST);
        toolBar.add(actionPanel, BorderLayout.WEST);
        topPanel.add(toolBar, BorderLayout.SOUTH);
        
        // --- CENTER PANEL (Table) ---
        String[] columns = {
            "ID", "Mã ngành", "Mã tổ hợp", "Môn 1", "HS 1", "Môn 2", "HS 2", "Môn 3", "HS 3", "TB Keys",
            "N1", "TO", "LI", "HO", "SI", "VA", "SU", "DI", "TI", "KHAC", "KTPL", "Độ lệch"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 10 && columnIndex <= 20) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
        };
        
        tableCombinations = new JTable(tableModel);
        tableCombinations.setRowHeight(30);
        tableCombinations.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableCombinations.setSelectionBackground(new Color(227, 242, 253));
        tableCombinations.setGridColor(AppColor.BORDER);
        
        tableCombinations.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnWidths();
        
        JTableHeader header = tableCombinations.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        JScrollPane scrollPane = new JScrollPane(tableCombinations);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
        
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scrollPane, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerCard, BorderLayout.CENTER);
        
        setupEvents();
    }
    
    private void setColumnWidths() {
        int[] widths = { 50, 100, 100, 60, 50, 60, 50, 60, 50, 140, 45, 45, 45, 45, 45, 45, 45, 45, 45, 55, 55, 80 };
        for (int i = 0; i < widths.length; i++) {
            if (i < tableCombinations.getColumnCount()) {
                tableCombinations.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        }
    }

    private void setupEvents() {
        btnSearch.addActionListener(e -> {
            currentSearchTerm = txtSearch.getText().trim();
            loadData();
        });
        txtSearch.addActionListener(e -> btnSearch.doClick());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); currentSearchTerm = ""; loadData(); });
        
        // Sự kiện Import
        btnImport.addActionListener(e -> importExcel());
    }
    
    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel Ngành - Tổ hợp");
        
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) { return f.getName().endsWith(".xlsx") || f.isDirectory(); }
            public String getDescription() { return "Excel files (*.xlsx)"; }
        });
        
        int userSelection = fileChooser.showOpenDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            SwingWorker<MajorCombinationService.ImportResult, Void> worker = new SwingWorker<>() {
                @Override
                protected MajorCombinationService.ImportResult doInBackground() throws Exception {
                    return service.importFromExcel(file);
                }

                @Override
                protected void done() {
                    try {
                        MajorCombinationService.ImportResult r = get();
                        String msg = "Import thành công " + r.successCount + " tổ hợp!";
                        
                        if (!r.errors.isEmpty()) {
                            msg += "\n\nCảnh báo/Lỗi dữ liệu:\n" + String.join("\n", r.errors.subList(0, Math.min(5, r.errors.size())));
                            if (r.errors.size() > 5) {
                                msg += "\n... và " + (r.errors.size() - 5) + " lỗi khác.";
                            }
                        }
                        
                        JOptionPane.showMessageDialog(MajorCombinationPanel.this, msg, "Kết quả Import", JOptionPane.INFORMATION_MESSAGE);
                        loadData(); 
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MajorCombinationPanel.this, "Lỗi khi import file: " + ex.getMessage(), "Lỗi Nghiêm Trọng", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        List<MajorCombination> list = currentSearchTerm.isEmpty() ? service.getAll() : service.search(currentSearchTerm);
        for (MajorCombination mc : list) {
            tableModel.addRow(new Object[]{
                mc.getId(), mc.getMaNganh(), mc.getMaToHop(), mc.getThMon1(), mc.getHsMon1(), 
                mc.getThMon2(), mc.getHsMon2(), mc.getThMon3(), mc.getHsMon3(), mc.getTbKeys(),
                mc.isN1(), mc.isToan(), mc.isLy(), mc.isHoa(), mc.isSinh(),
                mc.isVan(), mc.isSu(), mc.isDia(), mc.isTiengAnh(), mc.isKhac(), mc.isKtpl(),
                mc.getDoLech()
            });
        }
    }
}