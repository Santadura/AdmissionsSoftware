package com.tuyensinh.ui.aspiration;

import com.tuyensinh.service.AspirationService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class AspirationImportDialog extends JDialog {

    private final AspirationService service;
    private JTextField txtPath;
    private File selectedFile;
    private boolean imported = false;

    public AspirationImportDialog(JFrame parent, AspirationService service) {
        super(parent, "Import nguyện vọng từ Excel", true);
        this.service = service;
        initUI();
        setSize(550, 250);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(AppColor.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel form = new JPanel(new GridLayout(2, 1, 10, 10));
        form.setOpaque(false);

        txtPath = new JTextField();
        txtPath.setEditable(false);
        txtPath.setPreferredSize(new Dimension(300, 35));

        JButton btnBrowse = new RoundedButton("Chọn file", AppColor.PRIMARY, AppColor.PRIMARY_DARK);

        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setOpaque(false);
        filePanel.add(txtPath, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);

        JLabel lblInfo = new JLabel("Hỗ trợ file Excel (.xlsx) với các cột: CCCD, Thứ tự NV, Mã xét tuyển, Tuyển thẳng...");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(AppColor.TEXT_SECONDARY);

        form.add(new JLabel("Đường dẫn file Excel:"));
        form.add(filePanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton btnImport = new RoundedButton("Import ngay", new Color(67, 160, 71), new Color(46, 125, 50));
        JButton btnCancel = new RoundedButton("Hủy bỏ", Color.GRAY, Color.DARK_GRAY);

        buttonPanel.add(btnImport);
        buttonPanel.add(btnCancel);

        mainPanel.add(form, BorderLayout.NORTH);
        mainPanel.add(lblInfo, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        btnBrowse.addActionListener(e -> browse());
        btnImport.addActionListener(e -> importFile());
        btnCancel.addActionListener(e -> dispose());
    }

    private void browse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".xlsx") || f.isDirectory();
            }
            @Override
            public String getDescription() {
                return "Excel Workbook (*.xlsx)";
            }
        });

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            txtPath.setText(selectedFile.getAbsolutePath());
        }
    }

    private void importFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn file trước!");
            return;
        }

        // Find the Import button to disable it during processing
        JButton btnImport = null;
        JPanel bottomPanel = (JPanel) getContentPane().getComponent(2);
        for (Component c : bottomPanel.getComponents()) {
            if (c instanceof JButton && ((JButton) c).getText().equals("Import ngay")) {
                btnImport = (JButton) c;
                break;
            }
        }

        if (btnImport != null) btnImport.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<AspirationService.ImportResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AspirationService.ImportResult doInBackground() throws Exception {
                return service.importExcel(selectedFile);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    AspirationService.ImportResult result = get();
                    imported = true;

                    StringBuilder msg = new StringBuilder();
                    msg.append("Import hoàn tất!\n\n");
                    msg.append("• Tổng số dòng xử lý: ").append(result.total).append("\n");
                    msg.append("• Thành công: ").append(result.success).append("\n");
                    
                    if (!result.errors.isEmpty()) {
                        msg.append("• Lỗi: ").append(result.errors.size()).append(" dòng.\n\n");
                        msg.append("Chi tiết lỗi (tối đa 10 dòng đầu):\n");
                        for (int i = 0; i < Math.min(result.errors.size(), 10); i++) {
                            msg.append("  - ").append(result.errors.get(i)).append("\n");
                        }
                    }

                    JOptionPane.showMessageDialog(AspirationImportDialog.this, msg.toString(), "Kết quả Import", 
                        result.errors.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AspirationImportDialog.this, "Lỗi khi import: " + e.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    // Re-enable button if failed so user can try again
                    JButton finalBtn = null;
                    JPanel bp = (JPanel) getContentPane().getComponent(2);
                    for (Component c : bp.getComponents()) {
                        if (c instanceof JButton && ((JButton) c).getText().equals("Import ngay")) {
                            finalBtn = (JButton) c;
                            break;
                        }
                    }
                    if (finalBtn != null) finalBtn.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    public boolean isImported() {
        return imported;
    }
}
