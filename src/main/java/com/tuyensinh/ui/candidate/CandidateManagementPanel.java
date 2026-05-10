package com.tuyensinh.ui.candidate;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.service.CandidateService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.List;

public class CandidateManagementPanel extends JPanel {
    
    private JTable tableCandidates;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnSearch, btnImport, btnEdit, btnRefresh;
    private JButton btnPrev, btnNext;
    private JLabel lblPageInfo;
    
    private CandidateService service;
    private int currentPage = 0;
    private String currentSearchTerm = "";
    
    public CandidateManagementPanel() {
        this.service = new CandidateService();
        initUI();
        loadData();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Quản lý thí sinh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchBar.setBackground(AppColor.SURFACE);
        searchBar.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        btnSearch = new RoundedButton("Tìm kiếm", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnRefresh = new RoundedButton("Làm mới", new Color(100, 150, 200), new Color(70, 120, 170));
        
        searchBar.add(new JLabel("Tìm kiếm (CCCD/Họ tên):"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnRefresh);
        
        topPanel.add(searchBar, BorderLayout.SOUTH);
        
        String[] columns = {"ID", "CCCD", "SBD", "Họ tên", "Ngày sinh", "Giới tính", "ĐTƯT", "KVƯT", "Nơi sinh"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tableCandidates = new JTable(tableModel);
        tableCandidates.setRowHeight(30);
        tableCandidates.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableCandidates.setSelectionBackground(new Color(227, 242, 253));
        tableCandidates.setGridColor(AppColor.BORDER);
        
        JTableHeader header = tableCandidates.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        JScrollPane scrollPane = new JScrollPane(tableCandidates);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        
        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pagePanel.setOpaque(false);
        
        btnPrev = new RoundedButton("<< Trang trước", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnNext = new RoundedButton("Trang sau >>", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        lblPageInfo = new JLabel("Trang 1/1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        pagePanel.add(btnPrev);
        pagePanel.add(lblPageInfo);
        pagePanel.add(btnNext);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        btnImport = new RoundedButton("Import Excel", new Color(67, 160, 71), new Color(46, 125, 50));
        btnEdit = new RoundedButton("Sửa thông tin", new Color(251, 140, 0), new Color(239, 108, 0));
        
        actionPanel.add(btnImport);
        actionPanel.add(btnEdit);
        
        bottomPanel.add(pagePanel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerCard, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setupEvents();
    }
    
    private void setupEvents() {
        btnSearch.addActionListener(e -> {
            currentSearchTerm = txtSearch.getText().trim();
            currentPage = 0;
            loadData();
        });
        
        txtSearch.addActionListener(e -> btnSearch.doClick());
        
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            currentSearchTerm = "";
            currentPage = 0;
            loadData();
        });
        
        btnPrev.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });
        
        btnNext.addActionListener(e -> {
            long totalPages = getCurrentTotalPages();
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadData();
            }
        });
        
        btnImport.addActionListener(e -> importFromExcel());
        btnEdit.addActionListener(e -> editCandidate());
        
        tableCandidates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) editCandidate();
            }
        });
    }
    
    private void importFromExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel danh sách thí sinh");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".xlsx") || f.isDirectory();
            }
            public String getDescription() { return "Excel files (*.xlsx)"; }
        });
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            JDialog progressDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Đang import...", true);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressDialog.add(progressBar);
            progressDialog.setSize(300, 80);
            progressDialog.setLocationRelativeTo(this);
            
            SwingWorker<CandidateService.ImportResult, Void> worker = new SwingWorker<>() {
                @Override
                protected CandidateService.ImportResult doInBackground() throws Exception {
                    return service.importFromExcel(file);
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        CandidateService.ImportResult result = get();
                        String message = "Import thành công " + result.successCount + " thí sinh!";
                        if (!result.errors.isEmpty()) {
                            message += "\n\nCảnh báo (" + result.errors.size() + " lỗi):\n" + 
                                      String.join("\n", result.errors.subList(0, Math.min(10, result.errors.size())));
                            if (result.errors.size() > 10) message += "\n...và " + (result.errors.size() - 10) + " lỗi khác";
                        }
                        JOptionPane.showMessageDialog(CandidateManagementPanel.this, message);
                        loadData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(CandidateManagementPanel.this, 
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true);
        }
    }
    
    private void editCandidate() {
        int selectedRow = tableCandidates.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thí sinh để sửa");
            return;
        }
        
        Integer candidateId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Candidate candidate = service.getCandidateById(candidateId);
        
        if (candidate != null) {
            CandidateEditDialog dialog = new CandidateEditDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), candidate, service);
            if (dialog.showDialog()) {
                loadData();
            }
        }
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        
        List<Candidate> candidates = currentSearchTerm.isEmpty() ? 
            service.getCandidates(currentPage) : 
            service.searchCandidates(currentSearchTerm, currentPage);
        
        for (Candidate c : candidates) {
            tableModel.addRow(new Object[]{
                c.getIdthisinh(),
                c.getCccd(),
                c.getSobaodanh(),
                c.getHoTen(),
                c.getNgaySinh(),
                c.getGioiTinh(),
                c.getDoiTuong(),
                c.getKhuVuc(),
                c.getNoiSinh()
            });
        }
        
        updatePagination();
    }
    
    private void updatePagination() {
        long totalPages = getCurrentTotalPages();
        lblPageInfo.setText("Trang " + (currentPage + 1) + " / " + Math.max(1, totalPages));
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
    }
    
    private long getCurrentTotalPages() {
        return currentSearchTerm.isEmpty() ? 
            service.getTotalPages() : 
            service.getTotalSearchPages(currentSearchTerm);
    }
}