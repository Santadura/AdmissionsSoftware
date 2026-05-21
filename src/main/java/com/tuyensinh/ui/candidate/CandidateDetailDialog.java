package com.tuyensinh.ui.candidate;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.entity.CandidateScore;
import com.tuyensinh.service.CandidateScoreService;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CandidateDetailDialog extends JDialog {

    private final Candidate candidate;
    private final CandidateScoreService scoreService;

    public CandidateDetailDialog(JFrame parent, Candidate candidate) {
        super(parent, "Chi tiết thí sinh: " + candidate.getHoTen(), true);
        this.candidate = candidate;
        this.scoreService = new CandidateScoreService();

        initComponents();
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(AppColor.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Header Panel: Thông tin cơ bản ---
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // --- Center Panel: Điểm số ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        List<CandidateScore> scores = scoreService.getScoresByCccd(candidate.getCccd());

        tabbedPane.addTab("Điểm THPT", createScorePanel(scores, "THPT"));
        tabbedPane.addTab("Điểm ĐGNL", createScorePanel(scores, "DGNL"));
        tabbedPane.addTab("Điểm VSAT", createScorePanel(scores, "VSAT"));
        
        // Tab cho các phương thức khác nếu có
        List<String> otherMethods = scores.stream()
                .map(CandidateScore::getDPhuongthuc)
                .filter(pt -> pt != null && !pt.equals("THPT") && !pt.equals("DGNL") && !pt.equals("VSAT"))
                .distinct()
                .collect(Collectors.toList());
        
        for (String method : otherMethods) {
            tabbedPane.addTab("Điểm " + method, createScorePanel(scores, method));
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // --- Footer Panel: Nút đóng ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        JButton btnClose = new RoundedButton("Đóng", AppColor.PRIMARY, AppColor.PRIMARY_DARK);
        btnClose.addActionListener(e -> dispose());
        footerPanel.add(btnClose);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppColor.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cột 1 & 2
        addInfoField(panel, gbc, 0, 0, "CCCD:", candidate.getCccd());
        addInfoField(panel, gbc, 1, 0, "Họ tên:", candidate.getHoTen());
        addInfoField(panel, gbc, 2, 0, "Số báo danh:", candidate.getSobaodanh());
        addInfoField(panel, gbc, 3, 0, "Ngày sinh:", candidate.getNgaySinh() != null ? candidate.getNgaySinh().toString() : "---");

        // Cột 3 & 4
        addInfoField(panel, gbc, 0, 2, "Giới tính:", candidate.getGioiTinh());
        addInfoField(panel, gbc, 1, 2, "Nơi sinh:", candidate.getNoiSinh());
        addInfoField(panel, gbc, 2, 2, "ĐT ưu tiên:", candidate.getDoiTuong());
        addInfoField(panel, gbc, 3, 2, "KV ưu tiên:", candidate.getKhuVuc());

        // Cột 5 & 6
        addInfoField(panel, gbc, 0, 4, "Điện thoại:", candidate.getDienThoai());
        addInfoField(panel, gbc, 1, 4, "Email:", candidate.getEmail());
        addInfoField(panel, gbc, 2, 4, "Năm tuyển sinh:", String.valueOf(candidate.getNamTuyenSinh()));

        return panel;
    }

    private void addInfoField(JPanel panel, GridBagConstraints gbc, int row, int col, String label, String value) {
        // Label
        gbc.gridy = row;
        gbc.gridx = col;
        gbc.weightx = 0.1;
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLabel.setForeground(AppColor.TEXT_SECONDARY);
        panel.add(lblLabel, gbc);

        // Value
        gbc.gridx = col + 1;
        gbc.weightx = 0.2;
        JLabel lblValue = new JLabel(value != null && !value.isEmpty() ? value : "---");
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblValue.setForeground(AppColor.TEXT_PRIMARY);
        panel.add(lblValue, gbc);
    }

    private JPanel createScorePanel(List<CandidateScore> allScores, String method) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        List<CandidateScore> filtered = allScores.stream()
                .filter(s -> method.equalsIgnoreCase(s.getDPhuongthuc()))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            panel.add(new JLabel("Không có dữ liệu điểm cho phương thức này", SwingConstants.CENTER), BorderLayout.CENTER);
            return panel;
        }

        String[] columns = {"Môn học", "Điểm số"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        CandidateScore score = filtered.get(0); // Lấy bản ghi đầu tiên của phương thức này
        
        addScoreToModel(model, "Toán (TO)", score.getTo());
        addScoreToModel(model, "Vật lý (LI)", score.getLi());
        addScoreToModel(model, "Hóa học (HO)", score.getHo());
        addScoreToModel(model, "Sinh học (SI)", score.getSi());
        addScoreToModel(model, "Lịch sử (SU)", score.getSu());
        addScoreToModel(model, "Địa lý (DI)", score.getDi());
        addScoreToModel(model, "Ngữ văn (VA)", score.getVa());
        addScoreToModel(model, "Ngoại ngữ (N1_THI)", score.getN1Thi());
        addScoreToModel(model, "Ngoại ngữ CC (N1_CC)", score.getN1Cc());
        addScoreToModel(model, "Công nghệ (CNCN)", score.getCncn());
        addScoreToModel(model, "Tin học (CNNN)", score.getCnnn());
        addScoreToModel(model, "Tiếng Anh (TI)", score.getTi());
        addScoreToModel(model, "KT Pháp luật (KTPL)", score.getKtpl());
        addScoreToModel(model, "ĐGNL (NL1)", score.getNl1());
        addScoreToModel(model, "Năng khiếu 1 (NK1)", score.getNk1());
        addScoreToModel(model, "Năng khiếu 2 (NK2)", score.getNk2());

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addScoreToModel(DefaultTableModel model, String mon, BigDecimal value) {
        if (value != null && value.compareTo(BigDecimal.ZERO) >= 0) {
            model.addRow(new Object[]{mon, value});
        }
    }
}
