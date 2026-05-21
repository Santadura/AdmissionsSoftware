package com.tuyensinh.ui.bonus;

import com.tuyensinh.service.BonusScoreService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class EnglishBonusImportDialog extends JDialog {
    
    private BonusScoreService bonusService;
    private JTextField txtFilePath;
    private JTextArea txtPreview;
    private JButton btnBrowse;
    private JButton btnImport;
    private JButton btnCancel;
    private File selectedFile;
    private boolean imported = false;
    
    public EnglishBonusImportDialog(JFrame parent, BonusScoreService service) {
        super(parent, "Import quy đổi chứng chỉ Tiếng Anh từ Excel", true);
        this.bonusService = service;
        
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(600, 450);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(AppColor.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // File selection
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.setBackground(AppColor.SURFACE);
        filePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblFile = new JLabel("File Excel:");
        lblFile.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        txtFilePath = new JTextField(25);
        txtFilePath.setEditable(false);
        txtFilePath.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        btnBrowse = new RoundedButton("Chọn file", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        
        filePanel.add(lblFile);
        filePanel.add(txtFilePath);
        filePanel.add(btnBrowse);
        
        // Preview area
        txtPreview = new JTextArea();
        txtPreview.setEditable(false);
        txtPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtPreview.setText("Hướng dẫn file Excel quy đổi Tiếng Anh:\n" +
                "- Cột A: STT (bỏ qua)\n" +
                "- Cột B: CCCD\n" +
                "- Cột C: Chứng chỉ ngoại ngữ (IELTS, TOEFL...)\n" +
                "- Cột D: Điểm/Bậc chứng chỉ (6.5, B2...)\n" +
                "- Cột E: Điểm quy đổi (Quy đổi sang môn Anh thi THPT)\n" +
                "- Cột F: Điểm cộng (Điểm cộng CC vào tổng điểm xét tuyển)\n\n" +
                "Lưu ý:\n" +
                "1. Điểm quy đổi (Cột E) sẽ cập nhật vào bảng Điểm thi (mục n1Cc).\n" +
                "2. Điểm cộng (Cột F) sẽ cập nhật vào bảng Điểm cộng (mục Diem CC).");
        
        JScrollPane scrollPane = new JScrollPane(txtPreview);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        btnImport = new RoundedButton("Import", new Color(67, 160, 71), new Color(46, 125, 50));
        btnImport.setEnabled(false);
        
        btnCancel = new RoundedButton("Hủy", new Color(150, 150, 150), new Color(100, 100, 100));
        
        buttonPanel.add(btnImport);
        buttonPanel.add(btnCancel);
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Import kết quả quy đổi chứng chỉ ngoại ngữ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(filePanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Listeners
        btnBrowse.addActionListener(e -> browseFile());
        btnImport.addActionListener(e -> importFile());
        btnCancel.addActionListener(e -> dispose());
    }
    
    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel danh sách quy đổi tiếng anh");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return name.endsWith(".xlsx") || name.endsWith(".xls") || f.isDirectory();
            }
            public String getDescription() {
                return "Excel files (*.xlsx, *.xls)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            txtFilePath.setText(selectedFile.getAbsolutePath());
            btnImport.setEnabled(true);
            txtPreview.setText("File được chọn: " + selectedFile.getName() + "\nNhấn 'Import' để bắt đầu cập nhật điểm.");
        }
    }
    
    private void importFile() {
        if (selectedFile == null) return;
        
        btnImport.setEnabled(false);
        btnBrowse.setEnabled(false);
        txtPreview.setText("Đang xử lý quy đổi... Vui lòng đợi.\n");
        
        SwingWorker<BonusScoreService.ImportResult, String> worker = new SwingWorker<>() {
            @Override
            protected BonusScoreService.ImportResult doInBackground() throws Exception {
                return bonusService.importEnglishBonus(selectedFile);
            }
            
            @Override
            protected void done() {
                try {
                    BonusScoreService.ImportResult res = get();
                    StringBuilder sb = new StringBuilder();
                    sb.append("--- KẾT QUẢ QUY ĐỔI CC ---\n");
                    sb.append("Tổng số thí sinh xử lý: ").append(res.total).append("\n");
                    sb.append("Thành công: ").append(res.success).append("\n");
                    sb.append("Lỗi: ").append(res.errors.size()).append("\n\n");
                    
                    if (!res.errors.isEmpty()) {
                        sb.append("Chi tiết lỗi:\n");
                        for (String err : res.errors) {
                            sb.append("- ").append(err).append("\n");
                        }
                    }
                    
                    txtPreview.setText(sb.toString());
                    JOptionPane.showMessageDialog(EnglishBonusImportDialog.this, 
                            "Cập nhật điểm quy đổi hoàn tất!\nSố lượng: " + res.success + "/" + res.total);
                    imported = true;
                    btnCancel.setText("Đóng");
                } catch (Exception ex) {
                    txtPreview.append("\nLỖI: " + ex.getMessage());
                    JOptionPane.showMessageDialog(EnglishBonusImportDialog.this, "Lỗi khi import: " + ex.getMessage());
                } finally {
                    btnBrowse.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
    public boolean isImported() {
        return imported;
    }
}
