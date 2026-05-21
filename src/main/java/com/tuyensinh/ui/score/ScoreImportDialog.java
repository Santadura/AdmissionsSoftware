// src/main/java/com/tuyensinh/ui/score/ScoreImportDialog.java
package com.tuyensinh.ui.score;

import com.tuyensinh.service.CandidateScoreService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class ScoreImportDialog extends JDialog {

    private CandidateScoreService service;

    private JTextField txtPath;

    private JComboBox<String> cboLoai;

    private File selectedFile;

    private boolean imported = false;

    public ScoreImportDialog(
            JFrame parent,
            CandidateScoreService service
    ) {

        super(parent,
                "Import điểm từ Excel",
                true);

        this.service = service;

        initUI();

        setSize(500, 250);

        setLocationRelativeTo(parent);
    }

    private void initUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));

        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(3, 1, 10, 10));

        txtPath = new JTextField();
        txtPath.setEditable(false);

        JButton btnBrowse = new RoundedButton(
                "Chọn file",
                AppColor.PRIMARY,
                AppColor.PRIMARY_DARK
        );

        JPanel filePanel = new JPanel(new BorderLayout(10, 0));

        filePanel.add(txtPath, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);

        cboLoai = new JComboBox<>(new String[]{
                "Tất cả",
                "THPT",
                "VSAT",
                "DGNL"
        });

        form.add(new JLabel("File Excel"));
        form.add(filePanel);
        form.add(cboLoai);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnImport = new RoundedButton(
                "Import",
                new Color(67, 160, 71),
                new Color(46, 125, 50)
        );

        JButton btnCancel = new RoundedButton(
                "Hủy",
                Color.GRAY,
                Color.DARK_GRAY
        );

        buttonPanel.add(btnImport);
        buttonPanel.add(btnCancel);

        mainPanel.add(form, BorderLayout.CENTER);
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
                return f.getName().toLowerCase().endsWith(".xlsx")
                        || f.getName().toLowerCase().endsWith(".xls")
                        || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Excel (*.xlsx, *.xls)";
            }
        });

        if (chooser.showOpenDialog(this)
                == JFileChooser.APPROVE_OPTION) {

            selectedFile = chooser.getSelectedFile();

            txtPath.setText(
                    selectedFile.getAbsolutePath()
            );
        }
    }

    private void importFile() {

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn file");
            return;
        }

        JButton btnImport = (JButton) ((JPanel) getContentPane().getComponent(1)).getComponent(0);
        btnImport.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        final String loaiDiem = cboLoai.getSelectedItem().toString();

        // Use SwingWorker to run import in background
        SwingWorker<CandidateScoreService.ImportResult, Void> worker = new SwingWorker<>() {
            @Override
            protected CandidateScoreService.ImportResult doInBackground() throws Exception {
                return service.importExcel(selectedFile, loaiDiem);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnImport.setEnabled(true);
                try {
                    CandidateScoreService.ImportResult result = get();
                    imported = true;

                    StringBuilder msg = new StringBuilder();
                    msg.append("Import hoàn tất!\n");
                    msg.append("- Tổng số dòng: ").append(result.total).append("\n");
                    msg.append("- Thành công: ").append(result.success).append("\n");
                    
                    if (!result.errors.isEmpty()) {
                        msg.append("- Lỗi: ").append(result.errors.size()).append(" dòng.\n");
                        if (result.errors.size() <= 5) {
                            for (String err : result.errors) {
                                msg.append("  + ").append(err).append("\n");
                            }
                        } else {
                            msg.append("  (Chỉ hiển thị 5 lỗi đầu tiên)\n");
                            for (int i = 0; i < 5; i++) {
                                msg.append("  + ").append(result.errors.get(i)).append("\n");
                            }
                        }
                    }

                    JOptionPane.showMessageDialog(
                            ScoreImportDialog.this,
                            msg.toString(),
                            "Thông báo",
                            result.errors.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
                    );

                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            ScoreImportDialog.this,
                            "Lỗi: " + e.getMessage()
                    );
                }
            }
        };

        worker.execute();
    }

    public boolean showDialog() {

        setVisible(true);

        return imported;
    }
}