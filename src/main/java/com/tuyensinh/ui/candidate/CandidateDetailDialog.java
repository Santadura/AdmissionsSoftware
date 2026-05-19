package com.tuyensinh.ui.candidate;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.entity.CandidateScore;
import com.tuyensinh.repository.CandidateScoreRepository;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CandidateDetailDialog extends JDialog {

    private Candidate candidate;
    private List<CandidateScore> scores;

    public CandidateDetailDialog(JFrame parent, Candidate candidate) {
        super(parent, "CHI TIẾT THÍ SINH: " + candidate.getHoTen(), true);
        this.candidate = candidate;
        loadScores();
        initUI();
        setSize(850, 650);
        setLocationRelativeTo(parent);
    }

    private void loadScores() {
        CandidateScoreRepository repo = new CandidateScoreRepository();
        List<CandidateScore> all = repo.findAll();
        scores = new ArrayList<>();
        for (CandidateScore s : all) {
            if (s.getCccd() != null && s.getCccd().equals(candidate.getCccd())) {
                scores.add(s);
            }
        }
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(AppColor.BACKGROUND);
        main.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        infoPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(AppColor.PRIMARY, 1),
            "THÔNG TIN CÁ NHÂN",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13)
        ));
        infoPanel.setBackground(AppColor.SURFACE);

        addInfoRow(infoPanel, "CCCD:", candidate.getCccd());
        addInfoRow(infoPanel, "Số báo danh:", candidate.getSobaodanh());
        addInfoRow(infoPanel, "Họ và tên:", candidate.getHoTen());
        addInfoRow(infoPanel, "Ngày sinh:", candidate.getNgaySinh());
        addInfoRow(infoPanel, "Giới tính:", candidate.getGioiTinh());
        addInfoRow(infoPanel, "Nơi sinh:", candidate.getNoiSinh());
        addInfoRow(infoPanel, "Đối tượng ưu tiên:", formatDoiTuong(candidate.getDoiTuong()));
        addInfoRow(infoPanel, "Khu vực ưu tiên:", formatKhuVuc(candidate.getKhuVuc()));
        addInfoRow(infoPanel, "Email:", candidate.getEmail());
        addInfoRow(infoPanel, "Điện thoại:", candidate.getDienThoai());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        if (scores.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            JLabel emptyLabel = new JLabel("Chưa có dữ liệu điểm cho thí sinh này", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            tabbedPane.addTab("Chưa có điểm", emptyPanel);
        } else {
            for (CandidateScore score : scores) {
                String loaiDiem = score.getDPhuongthuc();
                if (loaiDiem == null || loaiDiem.isEmpty()) {
                    loaiDiem = "Điểm THPT";
                }
                JPanel scorePanel = createScorePanel(score);
                tabbedPane.addTab(loaiDiem, scorePanel);
            }
        }

        JButton btnClose = new RoundedButton("Đóng", Color.GRAY, Color.DARK_GRAY);
        btnClose.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnClose);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoPanel, tabbedPane);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);

        main.add(splitPane, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createScorePanel(CandidateScore score) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        List<Object[]> rows = new ArrayList<>();
        
        addScoreRow(rows, "Toán", score.getTo());
        addScoreRow(rows, "Ngữ văn", score.getVa());
        addScoreRow(rows, "Vật lý", score.getLi());
        addScoreRow(rows, "Hóa học", score.getHo());
        addScoreRow(rows, "Sinh học", score.getSi());
        addScoreRow(rows, "Lịch sử", score.getSu());
        addScoreRow(rows, "Địa lý", score.getDi());
        addScoreRow(rows, "Ngoại ngữ (thi THPT)", score.getN1Thi());
        addScoreRow(rows, "Ngoại ngữ (chứng chỉ)", score.getN1Cc());
        addScoreRow(rows, "Đánh giá năng lực", score.getNl1());
        addScoreRow(rows, "Tin học", score.getTi());
        addScoreRow(rows, "Kinh tế & Pháp luật", score.getKtpl());
        addScoreRow(rows, "Công nghệ (công nghiệp)", score.getCncn());
        addScoreRow(rows, "Công nghệ (nông nghiệp)", score.getCnnn());
        addScoreRow(rows, "Năng khiếu 1", score.getNk1());
        addScoreRow(rows, "Năng khiếu 2", score.getNk2());

        String[] columns = {"Môn thi", "Điểm số", "Ghi chú"};
        Object[][] data = rows.toArray(new Object[0][0]);
        
        JTable table = new JTable(data, columns);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(66, 133, 244));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void addScoreRow(List<Object[]> rows, String subject, BigDecimal value) {
        if (value != null && value.compareTo(BigDecimal.ZERO) != 0) {
            String displayValue = value.setScale(2, java.math.RoundingMode.HALF_UP).toString();
            rows.add(new Object[]{subject, displayValue, ""});
        }
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel val = new JLabel(value != null && !value.isEmpty() ? value : "—");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl);
        panel.add(val);
    }

    private String formatDoiTuong(String dt) {
        if (dt == null || dt.isEmpty()) return "Không";
        Map<String, String> map = new LinkedHashMap<>();
        map.put("01", "ĐT01: Con thương binh/bệnh binh");
        map.put("02", "ĐT02: Con người có công cách mạng");
        map.put("03", "ĐT03: Người dân tộc thiểu số");
        map.put("03b", "ĐT03b: Người dân tộc thiểu số (theo NĐ 57 nhóm B)");
        map.put("03c", "ĐT03c: Người dân tộc thiểu số (theo NĐ 57)");
        map.put("03d", "ĐT03d: Người dân tộc thiểu số (rất ít người)");
        map.put("04", "ĐT04: Người khuyết tật nặng");
        map.put("04a", "ĐT04a: Người khuyết tật đặc biệt nặng");
        map.put("04b", "ĐT04b: Người khuyết tật nặng");
        map.put("05", "ĐT05: Hộ nghèo/cận nghèo");
        map.put("05b", "ĐT05b: Hộ nghèo vùng dân tộc thiểu số");
        map.put("06", "ĐT06: Hoàn cảnh đặc biệt khó khăn");
        map.put("06a", "ĐT06a: Dân tộc thiểu số (rất ít người)");
        map.put("06b", "ĐT06b: Học sinh trường phổ thông dân tộc nội trú");
        map.put("06c", "ĐT06c: Học sinh trường dự bị đại học");
        map.put("07", "ĐT07: Hoàn thành nghĩa vụ quân sự");
        map.put("07a", "ĐT07a: Thanh niên xung phong");
        return map.getOrDefault(dt, "ĐT" + dt);
    }

    private String formatKhuVuc(String kv) {
        if (kv == null || kv.isEmpty()) return "Không";
        Map<String, String> map = new LinkedHashMap<>();
        map.put("1", "KV1: Vùng khó khăn nhất (miền núi, hải đảo)");
        map.put("2", "KV2: Vùng khó khăn");
        map.put("2NT", "KV2-NT: Vùng nông thôn");
        map.put("3", "KV3: Khu vực còn lại (thành phố, thị xã)");
        return map.getOrDefault(kv, "KV" + kv);
    }
}