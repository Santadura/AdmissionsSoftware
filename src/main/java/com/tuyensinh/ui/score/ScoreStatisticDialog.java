package com.tuyensinh.ui.score;

import com.tuyensinh.service.CandidateScoreService;
import com.tuyensinh.service.CandidateScoreService.ScoreStatisticRow;
import com.tuyensinh.ui.AppColor;
import com.tuyensinh.ui.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ScoreStatisticDialog extends JDialog {

    private CandidateScoreService service;

    private JComboBox<String> cboThongKeTheo;
    private JComboBox<String> cboLoaiDiem;
    private JComboBox<String> cboMon;

    private JTable table;
    private DefaultTableModel tableModel;

    private BarChartPanel chartPanel;

    private List<ScoreStatisticRow> currentRows = new ArrayList<>();

    public ScoreStatisticDialog(JFrame parent, CandidateScoreService service) {

        super(parent, "Thống kê điểm", true);

        this.service = service;

        initUI();
        loadStatistic();

        setSize(950, 650);
        setLocationRelativeTo(parent);
    }

    private void initUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(AppColor.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Thống kê điểm thí sinh");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(AppColor.TEXT_PRIMARY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(AppColor.SURFACE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER),
                new EmptyBorder(10, 10, 10, 10)
        ));

        cboThongKeTheo = new JComboBox<>(new String[]{
                "Theo loại điểm",
                "Theo môn"
        });

        cboLoaiDiem = new JComboBox<>(new String[]{
                "Tất cả",
                "THPT",
                "VSAT",
                "DGNL"
        });

        cboMon = new JComboBox<>(new String[]{
                "TO",
                "VA",
                "LI",
                "HO",
                "SI",
                "SU",
                "DI",
                "N1_THI",
                "N1_CC",
                "CNCN",
                "CNNN",
                "TI",
                "KTPL",
                "NL1",
                "NK1",
                "NK2",
                "NK3",
                "NK4"
        });

        JButton btnView = new RoundedButton(
                "Xem thống kê",
                AppColor.PRIMARY,
                AppColor.PRIMARY_DARK
        );

        filterPanel.add(new JLabel("Thống kê:"));
        filterPanel.add(cboThongKeTheo);

        filterPanel.add(new JLabel("Loại điểm:"));
        filterPanel.add(cboLoaiDiem);

        filterPanel.add(new JLabel("Môn:"));
        filterPanel.add(cboMon);

        filterPanel.add(btnView);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        chartPanel = new BarChartPanel();

        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setBackground(AppColor.SURFACE);
        chartWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColor.BORDER),
                new EmptyBorder(10, 10, 10, 10)
        ));
        chartWrapper.add(chartPanel, BorderLayout.CENTER);

        String[] columns = {
                "Loại điểm",
                "Môn",
                "Số lượng",
                "Điểm TB",
                "Điểm thấp nhất",
                "Điểm cao nhất"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(900, 220));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                chartWrapper,
                tableScroll
        );

        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);

        JButton btnClose = new RoundedButton(
                "Đóng",
                Color.GRAY,
                Color.DARK_GRAY
        );

        btnClose.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnClose);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        cboThongKeTheo.addActionListener(e -> updateFilterState());
        btnView.addActionListener(e -> loadStatistic());

        updateFilterState();
    }

    private void updateFilterState() {

        String type = cboThongKeTheo.getSelectedItem().toString();

        if ("Theo loại điểm".equals(type)) {

            cboMon.setEnabled(true);
            cboLoaiDiem.setEnabled(false);

        } else {

            cboMon.setEnabled(false);
            cboLoaiDiem.setEnabled(true);
        }
    }

    private void loadStatistic() {

        String type = cboThongKeTheo.getSelectedItem().toString();

        if ("Theo loại điểm".equals(type)) {

            String mon = cboMon.getSelectedItem().toString();

            currentRows = service.statisticByLoaiDiem(mon);

        } else {

            String loaiDiem = cboLoaiDiem.getSelectedItem().toString();

            currentRows = service.statisticByMon(loaiDiem);
        }

        loadTable();
        chartPanel.setRows(currentRows, type);
    }

    private void loadTable() {

        tableModel.setRowCount(0);

        for (ScoreStatisticRow row : currentRows) {

            tableModel.addRow(new Object[]{
                    row.getLoaiDiem(),
                    convertSubjectName(row.getMon()),
                    row.getSoLuong(),
                    formatNumber(row.getDiemTrungBinh()),
                    formatNumber(row.getDiemMin()),
                    formatNumber(row.getDiemMax())
            });
        }
    }

    private String formatNumber(BigDecimal value) {

        if (value == null) {
            return "";
        }

        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private String convertSubjectName(String code) {

        if (code == null) {
            return "";
        }

        switch (code) {

            case "TO":
                return "Toán";

            case "LI":
                return "Lý";

            case "HO":
                return "Hóa";

            case "SI":
                return "Sinh";

            case "SU":
                return "Sử";

            case "DI":
                return "Địa";

            case "VA":
                return "Văn";

            case "N1_THI":
                return "Ngoại ngữ thi";

            case "N1_CC":
                return "Ngoại ngữ CC";

            case "CNCN":
                return "Công nghệ CN";

            case "CNNN":
                return "Công nghệ NN";

            case "TI":
                return "Tin học";

            case "KTPL":
                return "KTPL";

            case "NL1":
                return "ĐGNL";

            case "NK1":
                return "Năng khiếu 1";

            case "NK2":
                return "Năng khiếu 2";

            case "NK3":
                return "Năng khiếu 3";

            case "NK4":
                return "Năng khiếu 4";

            default:
                return code;
        }
    }

    public void showDialog() {
        setVisible(true);
    }

    // ================= BAR CHART PANEL =================

    private class BarChartPanel extends JPanel {

        private List<ScoreStatisticRow> rows = new ArrayList<>();
        private String type = "Theo loại điểm";

        public BarChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(900, 320));
        }

        public void setRows(List<ScoreStatisticRow> rows, String type) {
            this.rows = rows;
            this.type = type;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            int left = 70;
            int right = 30;
            int top = 50;
            int bottom = 70;

            int chartWidth = width - left - right;
            int chartHeight = height - top - bottom;

            g2.setColor(AppColor.TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));

            String title = "Biểu đồ điểm trung bình";

            g2.drawString(title, left, 25);

            if (rows == null || rows.isEmpty()) {

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                g2.drawString("Không có dữ liệu thống kê", left, height / 2);
                return;
            }

            double maxValue = 0;

            for (ScoreStatisticRow row : rows) {
                if (row.getDiemTrungBinh() != null) {
                    maxValue = Math.max(
                            maxValue,
                            row.getDiemTrungBinh().doubleValue()
                    );
                }
            }

            if (maxValue <= 0) {
                maxValue = 10;
            }

            maxValue = Math.ceil(maxValue);

            g2.setColor(Color.LIGHT_GRAY);

            g2.drawLine(left, top, left, top + chartHeight);
            g2.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(Color.GRAY);

            for (int i = 0; i <= 5; i++) {

                int y = top + chartHeight - (i * chartHeight / 5);

                double value = maxValue * i / 5;

                g2.drawLine(left - 5, y, left + chartWidth, y);
                g2.drawString(String.format("%.1f", value), 15, y + 5);
            }

            int count = rows.size();

            int gap = 15;
            int barWidth = Math.max(25, (chartWidth - gap * (count + 1)) / count);

            for (int i = 0; i < count; i++) {

                ScoreStatisticRow row = rows.get(i);

                double avg = row.getDiemTrungBinh() == null
                        ? 0
                        : row.getDiemTrungBinh().doubleValue();

                int barHeight = (int) ((avg / maxValue) * chartHeight);

                int x = left + gap + i * (barWidth + gap);
                int y = top + chartHeight - barHeight;

                g2.setColor(new Color(66, 133, 244));
                g2.fillRect(x, y, barWidth, barHeight);

                g2.setColor(new Color(40, 90, 180));
                g2.drawRect(x, y, barWidth, barHeight);

                g2.setColor(AppColor.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));

                String valueText = row.getDiemTrungBinh()
                        .setScale(2, RoundingMode.HALF_UP)
                        .toString();

                g2.drawString(valueText, x, y - 5);

                String label;

                if ("Theo loại điểm".equals(type)) {
                    label = row.getLoaiDiem();
                } else {
                    label = convertSubjectName(row.getMon());
                }

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

                if (label.length() > 10) {
                    label = label.substring(0, 10);
                }

                g2.drawString(label, x, top + chartHeight + 20);
            }
        }
    }
}