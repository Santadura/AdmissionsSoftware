// src/main/java/com/tuyensinh/ui/candidate/CandidateImportDialog.java
package com.tuyensinh.ui.candidate;

import com.tuyensinh.service.CandidateService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class CandidateImportDialog extends JDialog {
    
    private CandidateService candidateService;
    private JTextField txtFilePath;
    private JTextArea txtPreview;
    private JButton btnBrowse;
    private JButton btnImport;
    private JButton btnCancel;
    private File selectedFile;
    private boolean imported = false;
    
    public CandidateImportDialog(JFrame parent, CandidateService service) {
        super(parent, "Import thí sinh từ Excel", true);
        this.candidateService = service;
        
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(500, 400);
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
        
        txtFilePath = new JTextField(20);
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
        
        JLabel lblTitle = new JLabel("Import danh sách thí sinh từ file Excel");
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
        fileChooser.setDialogTitle("Chọn file Excel danh sách thí sinh");
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
            
            // Preview
            try {
                StringBuilder preview = new StringBuilder();
                preview.append("File: ").append(selectedFile.getName()).append("\n");
                preview.append("Đang đọc dữ liệu...\n");
                // Có thể thêm preview các dòng đầu tiên ở đây
                txtPreview.setText(preview.toString());
                btnImport.setEnabled(true);
            } catch (Exception ex) {
                txtPreview.setText("Lỗi đọc file: " + ex.getMessage());
                btnImport.setEnabled(false);
            }
        }
    }
    
    private void importFile() {
        if (selectedFile == null) return;
        
        try {
            // Import logic sẽ được thêm vào đây
            JOptionPane.showMessageDialog(this, "Import thành công!");
            imported = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
    
    public boolean showDialog() {
        setVisible(true);
        return imported;
    }
}