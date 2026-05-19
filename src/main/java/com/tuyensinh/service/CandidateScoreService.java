package com.tuyensinh.service;

import com.tuyensinh.entity.CandidateScore;
import com.tuyensinh.repository.CandidateScoreRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CandidateScoreService {

    private final CandidateScoreRepository repository;

    public CandidateScoreService() {
        repository = new CandidateScoreRepository();
    }

    // ================= IMPORT =================

    public void importExcel(File file, String loaiDiem) throws Exception {

        List<CandidateScore> scores = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            int rowNum = 0;

            for (Row row : sheet) {

                rowNum++;

                if (rowNum == 1) {
                    continue;
                }

                CandidateScore score = new CandidateScore();

                score.setDPhuongthuc(loaiDiem);

                score.setCccd(getString(row, 0));
                score.setSobaodanh(getString(row, 1));

                score.setTo(getDecimal(row, 2));
                score.setLi(getDecimal(row, 3));
                score.setHo(getDecimal(row, 4));
                score.setSi(getDecimal(row, 5));
                score.setSu(getDecimal(row, 6));
                score.setDi(getDecimal(row, 7));
                score.setVa(getDecimal(row, 8));

                repository.save(score);
            }
        }
    }

    // ================= CRUD =================

    public List<CandidateScore> getAllScores() {
        return repository.findAll();
    }

    public CandidateScore findById(Integer id) {
        return repository.findById(id);
    }

    public void save(CandidateScore score) {
        repository.save(score);
    }

    public void update(CandidateScore score) {
        repository.update(score);
    }

    public void delete(Integer id) {
        repository.delete(id);
    }

    // ================= HELPER =================

    private String getString(Row row, int col) {

        Cell cell = row.getCell(col);

        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());

            default:
                return null;
        }
    }

    private BigDecimal getDecimal(Row row, int col) {

        Cell cell = row.getCell(col);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        try {
            return new BigDecimal(cell.getStringCellValue());
        } catch (Exception e) {
            return null;
        }
    }

    // ================= THỐNG KÊ =================

    public List<ScoreStatisticRow> statisticByLoaiDiem(String mon) {

        List<CandidateScore> scores = repository.findAll();

        java.util.Map<String, StatisticAccumulator> map =
                new java.util.LinkedHashMap<>();

        for (CandidateScore score : scores) {

            String loai = score.getDPhuongthuc();

            if (loai == null || loai.trim().isEmpty()) {
                continue;
            }

            BigDecimal value = getScoreBySubject(score, mon);

            if (!isValidScore(value)) {
                continue;
            }

            map.putIfAbsent(loai, new StatisticAccumulator(loai, mon));
            map.get(loai).add(value);
        }

        return toRows(map);
    }

    public List<ScoreStatisticRow> statisticByMon(String loaiDiem) {

        List<CandidateScore> scores = repository.findAll();

        java.util.Map<String, StatisticAccumulator> map =
                new java.util.LinkedHashMap<>();

        String[] subjects = {
                "TO", "LI", "HO", "SI", "SU", "DI", "VA",
                "N1_THI", "N1_CC", "CNCN", "CNNN",
                "TI", "KTPL", "NL1", "NK1", "NK2"
        };

        for (CandidateScore score : scores) {

            if (!"Tất cả".equalsIgnoreCase(loaiDiem)) {

                if (score.getDPhuongthuc() == null ||
                        !score.getDPhuongthuc().equalsIgnoreCase(loaiDiem)) {
                    continue;
                }
            }

            for (String mon : subjects) {

                BigDecimal value = getScoreBySubject(score, mon);

                if (!isValidScore(value)) {
                    continue;
                }

                map.putIfAbsent(mon, new StatisticAccumulator(
                        score.getDPhuongthuc(),
                        mon
                ));

                map.get(mon).add(value);
            }
        }

        return toRows(map);
    }

    private boolean isValidScore(BigDecimal value) {

        if (value == null) {
            return false;
        }

        // Bỏ 0.00 vì dữ liệu cũ đang dùng 0 như không có điểm
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal getScoreBySubject(CandidateScore score, String mon) {

        switch (mon) {

            case "TO":
                return score.getTo();

            case "LI":
                return score.getLi();

            case "HO":
                return score.getHo();

            case "SI":
                return score.getSi();

            case "SU":
                return score.getSu();

            case "DI":
                return score.getDi();

            case "VA":
                return score.getVa();

            case "N1_THI":
                return score.getN1Thi();

            case "N1_CC":
                return score.getN1Cc();

            case "CNCN":
                return score.getCncn();

            case "CNNN":
                return score.getCnnn();

            case "TI":
                return score.getTi();

            case "KTPL":
                return score.getKtpl();

            case "NL1":
                return score.getNl1();

            case "NK1":
                return score.getNk1();

            case "NK2":
                return score.getNk2();

            default:
                return null;
        }
    }

    private List<ScoreStatisticRow> toRows(
            java.util.Map<String, StatisticAccumulator> map
    ) {

        List<ScoreStatisticRow> rows = new ArrayList<>();

        for (StatisticAccumulator acc : map.values()) {
            rows.add(acc.toRow());
        }

        return rows;
    }

    public static class ScoreStatisticRow {

        private String loaiDiem;
        private String mon;
        private int soLuong;
        private BigDecimal diemTrungBinh;
        private BigDecimal diemMin;
        private BigDecimal diemMax;

        public ScoreStatisticRow(
                String loaiDiem,
                String mon,
                int soLuong,
                BigDecimal diemTrungBinh,
                BigDecimal diemMin,
                BigDecimal diemMax
        ) {
            this.loaiDiem = loaiDiem;
            this.mon = mon;
            this.soLuong = soLuong;
            this.diemTrungBinh = diemTrungBinh;
            this.diemMin = diemMin;
            this.diemMax = diemMax;
        }

        public String getLoaiDiem() {
            return loaiDiem;
        }

        public String getMon() {
            return mon;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public BigDecimal getDiemTrungBinh() {
            return diemTrungBinh;
        }

        public BigDecimal getDiemMin() {
            return diemMin;
        }

        public BigDecimal getDiemMax() {
            return diemMax;
        }
    }

    private static class StatisticAccumulator {

        private String loaiDiem;
        private String mon;

        private int count = 0;
        private BigDecimal sum = BigDecimal.ZERO;
        private BigDecimal min = null;
        private BigDecimal max = null;

        public StatisticAccumulator(String loaiDiem, String mon) {
            this.loaiDiem = loaiDiem;
            this.mon = mon;
        }

        public void add(BigDecimal value) {

            count++;
            sum = sum.add(value);

            if (min == null || value.compareTo(min) < 0) {
                min = value;
            }

            if (max == null || value.compareTo(max) > 0) {
                max = value;
            }
        }

        public ScoreStatisticRow toRow() {

            BigDecimal avg = BigDecimal.ZERO;

            if (count > 0) {
                avg = sum.divide(
                        BigDecimal.valueOf(count),
                        2,
                        java.math.RoundingMode.HALF_UP
                );
            }

            return new ScoreStatisticRow(
                    loaiDiem,
                    mon,
                    count,
                    avg,
                    min,
                    max
            );
        }
    }
}