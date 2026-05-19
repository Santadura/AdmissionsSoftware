package com.tuyensinh.ui.candidate;

import com.tuyensinh.repository.CandidateRepository;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CandidateStatisticDialog extends JDialog {

    private long totalCount;
    private Map<String, Long> doiTuongStats;
    private Map<String, Long> khuVucStats;

    public CandidateStatisticDialog(JFrame parent) {
        super(parent, "THỐNG KÊ THÍ SINH", true);
        loadData();
        initUI();
        setSize(950, 650);
        setLocationRelativeTo(parent);
    }

    private void loadData() {
        CandidateRepository repo = new CandidateRepository();
        totalCount = repo.countFast();
        doiTuongStats = repo.countByDoiTuong();
        khuVucStats = repo.countByKhuVuc();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(AppColor.BACKGROUND);
        main.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("THỐNG KÊ THÍ SINH");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColor.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel chartPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        chartPanel.setOpaque(false);

        JPanel totalPanel = createTotalStatPanel();
        chartPanel.add(totalPanel);

        JPanel doiTuongPanel = createPieChartPanel("ĐỐI TƯỢNG ƯU TIÊN", doiTuongStats, true);
        chartPanel.add(doiTuongPanel);

        JPanel khuVucPanel = createPieChartPanel("KHU VỰC ƯU TIÊN", khuVucStats, false);
        chartPanel.add(khuVucPanel);

        JButton btnClose = new RoundedButton("Đóng", Color.GRAY, Color.DARK_GRAY);
        btnClose.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnClose);

        main.add(title, BorderLayout.NORTH);
        main.add(chartPanel, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createTotalStatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColor.PRIMARY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("TỔNG SỐ THÍ SINH", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(AppColor.PRIMARY);

        JLabel total = new JLabel(String.valueOf(totalCount), SwingConstants.CENTER);
        total.setFont(new Font("Segoe UI", Font.BOLD, 48));
        total.setForeground(new Color(66, 133, 244));

        JLabel note = new JLabel("Đã nhập dữ liệu vào hệ thống", SwingConstants.CENTER);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(Color.GRAY);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(total, BorderLayout.CENTER);
        panel.add(note, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPieChartPanel(String chartTitle, Map<String, Long> data, boolean isDoiTuong) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(chartTitle, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(AppColor.TEXT_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);

        PieChartPanel pieChart = new PieChartPanel(data);
        pieChart.setPreferredSize(new Dimension(200, 180));
        panel.add(pieChart, BorderLayout.CENTER);

        JPanel legend = new JPanel(new GridLayout(0, 1, 5, 3));
        legend.setBackground(Color.WHITE);
        legend.setBorder(new EmptyBorder(10, 10, 5, 10));

        Color[] colors = {
            new Color(66, 133, 244),
            new Color(234, 67, 53),
            new Color(52, 168, 83),
            new Color(251, 188, 5),
            new Color(128, 0, 128),
            new Color(255, 102, 0),
            new Color(0, 150, 136),
            new Color(121, 85, 72)
        };

        int idx = 0;
        Map<String, Long> sortedData = new LinkedHashMap<>();
        data.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .forEachOrdered(x -> sortedData.put(x.getKey(), x.getValue()));

        for (Map.Entry<String, Long> entry : sortedData.entrySet()) {
            String displayKey = isDoiTuong ? formatDoiTuong(entry.getKey()) : formatKhuVuc(entry.getKey());
            
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            item.setBackground(Color.WHITE);
            
            JLabel colorBox = new JLabel("■");
            colorBox.setForeground(colors[idx % colors.length]);
            colorBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            JLabel label = new JLabel(displayKey + " (" + entry.getValue() + ")");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            
            item.add(colorBox);
            item.add(label);
            legend.add(item);
            idx++;
        }

        if (data == null || data.isEmpty()) {
            JLabel empty = new JLabel("Không có dữ liệu", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            legend.add(empty);
        }

        JScrollPane legendScroll = new JScrollPane(legend);
        legendScroll.setBorder(null);
        legendScroll.setPreferredSize(new Dimension(280, 150));
        
        panel.add(legendScroll, BorderLayout.SOUTH);
        
        return panel;
    }

    private String formatDoiTuong(String code) {
        if (code == null || code.isEmpty()) return "Không xác định";
        Map<String, String> map = new LinkedHashMap<>();
        map.put("01", "ĐT01: Con thương binh/bệnh binh");
        map.put("02", "ĐT02: Con người có công cách mạng");
        map.put("03", "ĐT03: Người dân tộc thiểu số");
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
        map.put("03b", "ĐT03b: Người dân tộc thiểu số (theo NĐ 57 nhóm B)");
        return map.getOrDefault(code, "ĐT" + code);
    }

    private String formatKhuVuc(String code) {
        if (code == null || code.isEmpty()) return "Không xác định";
        Map<String, String> map = new LinkedHashMap<>();
        map.put("1", "KV1: Vùng khó khăn nhất (miền núi, hải đảo)");
        map.put("2", "KV2: Vùng khó khăn");
        map.put("2NT", "KV2-NT: Vùng nông thôn");
        map.put("3", "KV3: Khu vực còn lại (thành phố, thị xã)");
        return map.getOrDefault(code, "KV" + code);
    }

    class PieChartPanel extends JPanel {
        private Map<String, Long> data;

        public PieChartPanel(Map<String, Long> data) {
            this.data = data;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.drawString("Không có dữ liệu", 50, 80);
                return;
            }

            long total = data.values().stream().mapToLong(Long::longValue).sum();
            if (total == 0) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w - 40, h - 40);
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            double startAngle = 0;
            Color[] colors = {
                new Color(66, 133, 244),
                new Color(234, 67, 53),
                new Color(52, 168, 83),
                new Color(251, 188, 5),
                new Color(128, 0, 128),
                new Color(255, 102, 0),
                new Color(0, 150, 136),
                new Color(121, 85, 72)
            };
            int idx = 0;

            for (Map.Entry<String, Long> entry : data.entrySet()) {
                double angle = 360.0 * entry.getValue() / total;
                g2.setColor(colors[idx % colors.length]);
                g2.fillArc(x, y, size, size, (int) startAngle, (int) Math.max(1, angle));
                startAngle += angle;
                idx++;
            }

            g2.setColor(Color.GRAY);
            g2.drawOval(x, y, size, size);
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(AppColor.TEXT_PRIMARY);
            String totalStr = String.valueOf(total);
            FontMetrics fm = g2.getFontMetrics();
            int strWidth = fm.stringWidth(totalStr);
            g2.drawString(totalStr, x + (size - strWidth) / 2, y + size / 2 + 5);
        }
    }

    public void showDialog() {
        setVisible(true);
    }
}