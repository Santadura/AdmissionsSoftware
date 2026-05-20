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

    public String toLoaiCode(String loaiText) {

        if (loaiText == null) {
            return null;
        }

        switch (loaiText) {
            case "THPT":
                return "3";
            case "DGNL":
            case "ĐGNL":
                return "4";
            case "VSAT":
                return "5";
            default:
                return loaiText;
        }
    }

    public String toLoaiText(String loaiCode) {

        if (loaiCode == null) {
            return "";
        }

        switch (loaiCode) {
            case "3":
                return "THPT";
            case "4":
                return "DGNL";
            case "5":
                return "VSAT";
            default:
                return loaiCode;
        }
    }

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

                score.setDPhuongthuc(toLoaiCode(loaiDiem));

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

            map.putIfAbsent(loai, new StatisticAccumulator(toLoaiText(loai), mon));
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

            String loaiCode = toLoaiCode(loaiDiem);

            if (!"Tất cả".equalsIgnoreCase(loaiDiem)) {

                if (score.getDPhuongthuc() == null ||
                        !score.getDPhuongthuc().equalsIgnoreCase(loaiCode)) {
                    continue;
                }
            }

            for (String mon : subjects) {

                BigDecimal value = getScoreBySubject(score, mon);

                if (!isValidScore(value)) {
                    continue;
                }

                map.putIfAbsent(mon, new StatisticAccumulator(
                        "Tất cả".equalsIgnoreCase(loaiDiem)
                                ? "Tất cả"
                                : toLoaiText(score.getDPhuongthuc()),
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
    public boolean existsByCccdAndLoai(String cccd, String loaiText) {

        return repository.existsByCccdAndLoai(
                cccd,
                toLoaiCode(loaiText)
        );
    }

    public void importThptFromCandidateExcel(File file) throws Exception {

        try (FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            int rowNum = 0;

            for (Row row : sheet) {

                rowNum++;

                if (rowNum == 1) {
                    continue;
                }

                String cccd = getString(row, 1); // CCCD

                if (cccd == null || cccd.trim().isEmpty()) {
                    continue;
                }

                CandidateScore score = new CandidateScore();

                score.setCccd(cccd);
                score.setSobaodanh(cccd);
                score.setDPhuongthuc("3"); // THPT

                score.setTo(getDecimal(row, 7));       // TO
                score.setVa(getDecimal(row, 8));       // VA
                score.setLi(getDecimal(row, 9));       // LI
                score.setHo(getDecimal(row, 10));      // HO
                score.setSi(getDecimal(row, 11));      // SI
                score.setSu(getDecimal(row, 12));      // SU
                score.setDi(getDecimal(row, 13));      // DI

                BigDecimal gdcd = getDecimal(row, 14); // GDCD
                BigDecimal ktpl = getDecimal(row, 17); // KTPL

                // DB không có cột GDCD riêng, nên gom vào KTPL
                score.setKtpl(max(gdcd, ktpl));

                score.setN1Thi(getDecimal(row, 15));   // NN
                score.setN1Cc(getDecimal(row, 15));    // tạm lấy bằng NN

                score.setTi(getDecimal(row, 18));      // TI
                score.setCncn(getDecimal(row, 19));    // CNCN
                score.setCnnn(getDecimal(row, 20));    // CNNN

                score.setNk1(getDecimal(row, 22));     // NK1
                score.setNk2(getDecimal(row, 23));     // NK2
                score.setNk3(getDecimal(row, 24));     // NK3
                score.setNk4(getDecimal(row, 25));     // NK4

                repository.saveOrUpdateByCccdAndLoai(score);
            }
        }
    }

    public void importVsaTDgnlExcel(File file) throws Exception {

        try (FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet vsatSheet = workbook.getSheet("VSAT");
            if (vsatSheet == null) {
                vsatSheet = workbook.getSheetAt(0);
            }

            Sheet dgnlSheet = workbook.getSheet("DGNL");
            if (dgnlSheet == null && workbook.getNumberOfSheets() > 1) {
                dgnlSheet = workbook.getSheetAt(1);
            }

            if (vsatSheet != null) {
                importVsatSheet(vsatSheet);
            }

            if (dgnlSheet != null) {
                importDgnlSheet(dgnlSheet);
            }
        }
    }

    private void importVsatSheet(Sheet sheet) {

        java.util.Map<String, CandidateScore> map = new java.util.LinkedHashMap<>();

        int rowNum = 0;

        for (Row row : sheet) {

            rowNum++;

            if (rowNum == 1) {
                continue;
            }

            String cccd = getString(row, 1);       // CMND
            String maMonThi = getString(row, 6);   // MAMONTHI
            BigDecimal diem = getDecimal(row, 8);  // DIEM

            if (cccd == null || cccd.trim().isEmpty()) {
                continue;
            }

            if (maMonThi == null || diem == null) {
                continue;
            }

            CandidateScore score = map.get(cccd);

            if (score == null) {
                score = new CandidateScore();
                score.setCccd(cccd);
                score.setSobaodanh(cccd);
                score.setDPhuongthuc("5"); // VSAT
                map.put(cccd, score);
            }

            applyVsatScore(score, maMonThi, diem);
        }

        for (CandidateScore score : map.values()) {

            CandidateScore existing = repository.findByCccdAndLoai(
                    score.getCccd(),
                    "5"
            );

            if (existing != null) {
                mergeMaxScore(existing, score);
                repository.update(existing);
            } else {
                repository.save(score);
            }
        }
    }

    private void importDgnlSheet(Sheet sheet) {

        java.util.Map<String, CandidateScore> map = new java.util.LinkedHashMap<>();

        int rowNum = 0;

        for (Row row : sheet) {

            rowNum++;

            if (rowNum == 1) {
                continue;
            }

            String cccd = getString(row, 1);       // CMND
            BigDecimal diem = getDecimal(row, 8);  // DIEM

            if (cccd == null || cccd.trim().isEmpty()) {
                continue;
            }

            if (diem == null) {
                continue;
            }

            CandidateScore score = map.get(cccd);

            if (score == null) {
                score = new CandidateScore();
                score.setCccd(cccd);
                score.setSobaodanh(cccd);
                score.setDPhuongthuc("4"); // DGNL
                map.put(cccd, score);
            }

            score.setNl1(max(score.getNl1(), diem));
        }

        for (CandidateScore score : map.values()) {

            CandidateScore existing = repository.findByCccdAndLoai(
                    score.getCccd(),
                    "4"
            );

            if (existing != null) {
                existing.setNl1(max(existing.getNl1(), score.getNl1()));
                repository.update(existing);
            } else {
                repository.save(score);
            }
        }
    }

    private void applyVsatScore(
            CandidateScore score,
            String maMonThi,
            BigDecimal diem
    ) {

        String mon = maMonThi.trim().toUpperCase();

        switch (mon) {

            case "TO_VS":
            case "M1":
                score.setTo(max(score.getTo(), diem));
                break;

            case "LI_VS":
            case "M2":
                score.setLi(max(score.getLi(), diem));
                break;

            case "HO_VS":
            case "M3":
                score.setHo(max(score.getHo(), diem));
                break;

            case "SI_VS":
            case "M4":
                score.setSi(max(score.getSi(), diem));
                break;

            case "VA_VS":
            case "M5":
                score.setVa(max(score.getVa(), diem));
                break;

            case "SU_VS":
            case "M6":
                score.setSu(max(score.getSu(), diem));
                break;

            case "DI_VS":
            case "M7":
                score.setDi(max(score.getDi(), diem));
                break;

            case "N1_VS":
            case "M8":
                score.setN1Thi(max(score.getN1Thi(), diem));
                score.setN1Cc(max(score.getN1Cc(), diem));
                break;

            case "TI_VS":
            case "M9":
                score.setTi(max(score.getTi(), diem));
                break;

            case "KTPL_VS":
            case "M10":
                score.setKtpl(max(score.getKtpl(), diem));
                break;
        }
    }

    private void mergeMaxScore(CandidateScore existing, CandidateScore imported) {

        existing.setTo(max(existing.getTo(), imported.getTo()));
        existing.setLi(max(existing.getLi(), imported.getLi()));
        existing.setHo(max(existing.getHo(), imported.getHo()));
        existing.setSi(max(existing.getSi(), imported.getSi()));
        existing.setSu(max(existing.getSu(), imported.getSu()));
        existing.setDi(max(existing.getDi(), imported.getDi()));
        existing.setVa(max(existing.getVa(), imported.getVa()));

        existing.setN1Thi(max(existing.getN1Thi(), imported.getN1Thi()));
        existing.setN1Cc(max(existing.getN1Cc(), imported.getN1Cc()));

        existing.setCncn(max(existing.getCncn(), imported.getCncn()));
        existing.setCnnn(max(existing.getCnnn(), imported.getCnnn()));
        existing.setTi(max(existing.getTi(), imported.getTi()));
        existing.setKtpl(max(existing.getKtpl(), imported.getKtpl()));

        existing.setNl1(max(existing.getNl1(), imported.getNl1()));

        existing.setNk1(max(existing.getNk1(), imported.getNk1()));
        existing.setNk2(max(existing.getNk2(), imported.getNk2()));
        existing.setNk3(max(existing.getNk3(), imported.getNk3()));
        existing.setNk4(max(existing.getNk4(), imported.getNk4()));
    }

    private BigDecimal max(BigDecimal oldValue, BigDecimal newValue) {

        if (newValue == null) {
            return oldValue;
        }

        if (oldValue == null) {
            return newValue;
        }

        return newValue.compareTo(oldValue) > 0 ? newValue : oldValue;
    }
}