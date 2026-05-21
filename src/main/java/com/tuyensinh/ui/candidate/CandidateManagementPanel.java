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
    private JButton btnSearch, btnImport, btnAdd, btnEdit, btnRefresh, btnView, btnCert, btnDelete;
    private JScrollPane scrollPane;
    
    private JPanel statsContainer;
    private JLabel lblTotalCount;
    private JPanel pnlObjectStats;
    private JPanel pnlRegionStats;
    
    private CandidateService service;
    private int currentPage = 0;
    private String currentSearchTerm = "";
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    
    public CandidateManagementPanel() {
        this.service = new CandidateService();
        initUI();
        loadData(true);
        loadStats();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Quản lý thí sinh 2025");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(AppColor.TEXT_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        
        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchBar.setBackground(AppColor.SURFACE);
        searchBar.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        btnSearch = new RoundedButton("Tìm kiếm", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnRefresh = new RoundedButton("Làm mới", new Color(100, 150, 200), new Color(70, 120, 170));
        
        searchBar.add(new JLabel("Tìm kiếm:"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnRefresh);
        
        // Statistics section
        statsContainer = new JPanel(new GridLayout(1, 3, 15, 0));
        statsContainer.setOpaque(false);
        statsContainer.setPreferredSize(new Dimension(0, 140)); // Fixed height for stats
        
        lblTotalCount = new JLabel("0", SwingConstants.CENTER);
        statsContainer.add(createStatCard("TỔNG THÍ SINH", lblTotalCount, AppColor.PRIMARY));
        
        pnlObjectStats = new JPanel();
        pnlObjectStats.setLayout(new BoxLayout(pnlObjectStats, BoxLayout.Y_AXIS));
        pnlObjectStats.setOpaque(false);
        statsContainer.add(createStatCard("THEO ĐỐI TƯỢNG", pnlObjectStats, new Color(76, 175, 80)));
        
        pnlRegionStats = new JPanel();
        pnlRegionStats.setLayout(new BoxLayout(pnlRegionStats, BoxLayout.Y_AXIS));
        pnlRegionStats.setOpaque(false);
        statsContainer.add(createStatCard("THEO KHU VỰC", pnlRegionStats, new Color(255, 152, 0)));

        JPanel topContent = new JPanel(new BorderLayout(10, 10));
        topContent.setOpaque(false);
        topContent.add(searchBar, BorderLayout.NORTH);
        topContent.add(statsContainer, BorderLayout.CENTER);
        
        topPanel.add(topContent, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // ... (Table initialization remains same)
        String[] columns = {"ID", "CCCD", "SBD", "Họ tên", "Ngày sinh", "Năm TS", "Giới tính", "ĐTƯT", "KVƯT", "Nơi sinh"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tableCandidates = new JTable(tableModel);
        tableCandidates.setRowHeight(30);
        tableCandidates.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableCandidates.setSelectionBackground(new Color(227, 242, 253));
        tableCandidates.setGridColor(AppColor.BORDER);
        tableCandidates.getColumnModel().getColumn(0).setMaxWidth(50);
        tableCandidates.getColumnModel().getColumn(5).setMaxWidth(60);
        
        JTableHeader header = tableCandidates.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        scrollPane = new JScrollPane(tableCandidates);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(AppColor.SURFACE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));
        centerCard.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        btnView = new RoundedButton("Xem chi tiết", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnCert = new RoundedButton("Thêm chứng chỉ", new Color(156, 39, 176), new Color(123, 31, 162));
        btnImport = new RoundedButton("Import Excel", new Color(67, 160, 71), new Color(46, 125, 50));
        btnAdd = new RoundedButton("Thêm mới", new Color(0, 150, 136), new Color(0, 121, 107));
        btnEdit = new RoundedButton("Sửa thông tin", new Color(251, 140, 0), new Color(239, 108, 0));
        btnDelete = new RoundedButton("Xóa", new Color(211, 47, 47), new Color(183, 28, 28));
        
        actionPanel.add(btnView);
        actionPanel.add(btnCert);
        actionPanel.add(btnImport);
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        
        add(centerCard, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setupEvents();
    }

    private JPanel createStatCard(String title, JComponent content, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(AppColor.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(AppColor.TEXT_SECONDARY);
        
        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(3, 0));
        accentBar.setBackground(accentColor);

        if (content instanceof JLabel) {
            ((JLabel) content).setFont(new Font("Segoe UI", Font.BOLD, 24));
            ((JLabel) content).setForeground(accentColor);
            card.add(content, BorderLayout.CENTER);
        } else {
            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
            card.add(scroll, BorderLayout.CENTER);
        }

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(accentBar, BorderLayout.WEST);

        return card;
    }

    private void loadStats() {
        long total = service.getTotalCandidates();
        lblTotalCount.setText(String.valueOf(total));

        updateStatList(pnlObjectStats, service.getStatisticsByObject());
        updateStatList(pnlRegionStats, service.getStatisticsByRegion());
        
        statsContainer.revalidate();
        statsContainer.repaint();
    }

    private void updateStatList(JPanel panel, List<Object[]> stats) {
        panel.removeAll();
        for (Object[] row : stats) {
            String name = row[0] != null ? row[0].toString() : "Chưa xác định";
            String count = row[1].toString();
            
            JPanel item = new JPanel(new BorderLayout());
            item.setOpaque(false);
            
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            JLabel lblCount = new JLabel(count);
            lblCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            item.add(lblName, BorderLayout.WEST);
            item.add(lblCount, BorderLayout.EAST);
            panel.add(item);
        }
    }
    
    private void setupEvents() {
        btnSearch.addActionListener(e -> {
            currentSearchTerm = txtSearch.getText().trim();
            currentPage = 0;
            hasMoreData = true;
            loadData(true);
        });
        
        txtSearch.addActionListener(e -> btnSearch.doClick());
        
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            currentSearchTerm = "";
            currentPage = 0;
            hasMoreData = true;
            loadData(true);
            loadStats();
        });

        // Infinite Scroll Listener
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!isLoading && hasMoreData) {
                JScrollBar scrollBar = (JScrollBar) e.getSource();
                int extent = scrollBar.getModel().getExtent();
                int maximum = scrollBar.getModel().getMaximum();
                int value = scrollBar.getValue();
                if (value + extent > maximum - 50) { // Sắp chạm đáy
                    currentPage++;
                    loadData(false);
                }
            }
        });
        
        btnImport.addActionListener(e -> importFromExcel());
        btnAdd.addActionListener(e -> addCandidate());
        btnEdit.addActionListener(e -> editCandidate());
        btnDelete.addActionListener(e -> deleteCandidate());
        btnView.addActionListener(e -> viewCandidate());
        btnCert.addActionListener(e -> addCertificate());
        
        tableCandidates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) viewCandidate();
            }
        });
    }

    private void addCandidate() {
        Candidate candidate = new Candidate();
        CandidateEditDialog dialog = new CandidateEditDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), candidate, service);
        if (dialog.showDialog()) {
            loadData(true);
            loadStats();
        }
    }

    private void addCertificate() {
        int selectedRow = tableCandidates.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thí sinh để thêm chứng chỉ");
            return;
        }
        
        Integer candidateId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Candidate candidate = service.getCandidateById(candidateId);
        
        if (candidate != null) {
            CertificateEntryDialog dialog = new CertificateEntryDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), candidate);
            dialog.setVisible(true);
        }
    }

    private void viewCandidate() {
        int selectedRow = tableCandidates.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thí sinh để xem");
            return;
        }
        
        Integer candidateId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Candidate candidate = service.getCandidateById(candidateId);
        
        if (candidate != null) {
            CandidateDetailDialog dialog = new CandidateDetailDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), candidate);
            dialog.setVisible(true);
        }
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
                        loadData(true);
                        loadStats();
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
                loadData(true);
                loadStats();
            }
        }
    }

    private void deleteCandidate() {
        int selectedRow = tableCandidates.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thí sinh để xóa");
            return;
        }

        Integer candidateId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String candidateName = (String) tableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa thí sinh: " + candidateName + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Candidate candidate = service.getCandidateById(candidateId);
                if (candidate != null) {
                    service.deleteCandidate(candidate);
                    JOptionPane.showMessageDialog(this, "Đã xóa thí sinh thành công!");
                    loadData(true);
                    loadStats();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa thí sinh: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadData(boolean clearTable) {
        if (isLoading) return;
        isLoading = true;

        if (clearTable) {
            tableModel.setRowCount(0);
            currentPage = 0;
            hasMoreData = true;
        }
        
        SwingWorker<List<Candidate>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Candidate> doInBackground() {
                return currentSearchTerm.isEmpty() ? 
                    service.getCandidates(currentPage) : 
                    service.searchCandidates(currentSearchTerm, currentPage);
            }

            @Override
            protected void done() {
                try {
                    List<Candidate> candidates = get();
                    if (candidates.isEmpty()) {
                        hasMoreData = false;
                    } else {
                        for (Candidate c : candidates) {
                            tableModel.addRow(new Object[]{
                                c.getIdthisinh(),
                                c.getCccd(),
                                c.getSobaodanh(),
                                c.getHoTen(),
                                c.getNgaySinh(),
                                c.getNamTuyenSinh(),
                                c.getGioiTinh(),
                                c.getDoiTuong(),
                                c.getKhuVuc(),
                                c.getNoiSinh()
                            });
                        }
                        if (candidates.size() < 20) { // Giả sử PAGE_SIZE = 20
                            hasMoreData = false;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    isLoading = false;
                }
            }
        };
        worker.execute();
    }
}