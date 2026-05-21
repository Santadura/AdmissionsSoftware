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
                "THPT - File DS thí sinh",
                "VSAT + DGNL - File điểm riêng"
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
                return f.getName().endsWith(".xlsx")
                        || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Excel (*.xlsx)";
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

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn file"
            );

            return;
        }

        String type = cboLoai.getSelectedItem().toString();

        JDialog progressDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Đang import điểm...",
                true
        );

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblMessage = new JLabel("Đang xử lý file Excel, vui lòng chờ...");
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(lblMessage, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        progressDialog.setContentPane(panel);
        progressDialog.setSize(420, 130);
        progressDialog.setLocationRelativeTo(this);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {

                if (type.startsWith("THPT")) {

                    service.importThptFromCandidateExcel(selectedFile);

                } else if (type.startsWith("VSAT")) {

                    service.importVsaTDgnlExcel(selectedFile);
                }

                return null;
            }

            @Override
            protected void done() {

                progressDialog.dispose();

                try {

                    get();

                    imported = true;

                    JOptionPane.showMessageDialog(
                            ScoreImportDialog.this,
                            "Import điểm thành công!"
                    );

                    dispose();

                } catch (Exception e) {

                    JOptionPane.showMessageDialog(
                            ScoreImportDialog.this,
                            "Lỗi import: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();

        progressDialog.setVisible(true);
    }

    public boolean showDialog() {

        setVisible(true);

        return imported;
    }
}