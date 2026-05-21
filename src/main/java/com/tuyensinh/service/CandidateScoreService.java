package com.tuyensinh.service;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.entity.CandidateScore;
import com.tuyensinh.repository.CandidateRepository;
import com.tuyensinh.repository.CandidateScoreRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateScoreService {

    private final CandidateScoreRepository repository;
    private final CandidateRepository candidateRepo;

    public CandidateScoreService() {
        repository = new CandidateScoreRepository();
        candidateRepo = new CandidateRepository();
    }

    // ================= IMPORT =================

    public static class ImportResult {
        public final int total;
        public final int success;
        public final List<String> errors;

        public ImportResult(int total, int success, List<String> errors) {
            this.total = total;
            this.success = success;
            this.errors = errors;
        }
    }

    public ImportResult importExcel(File file, String loaiDiem) throws Exception {

        List<CandidateScore> scoresBatch = new ArrayList<>();
        List<Candidate> candidatesBatch = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        // Map to group scores by CCCD (for long format/multiple rows per candidate)
        Map<String, CandidateScore> consolidatedScores = new HashMap<>();
        Map<String, Candidate> consolidatedCandidates = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            for (int sIdx = 0; sIdx < workbook.getNumberOfSheets(); sIdx++) {
                Sheet sheet = workbook.getSheetAt(sIdx);
                if (sheet.getPhysicalNumberOfRows() == 0) continue;

                Row headerRow = sheet.getRow(0);
                if (headerRow == null) continue;

                Map<String, Integer> headerMap = new HashMap<>();
                for (Cell cell : headerRow) {
                    if (cell != null) {
                        String header = getCellString(cell).toUpperCase();
                        if (!header.isEmpty()) headerMap.put(header, cell.getColumnIndex());
                    }
                }

                int cccdCol = findColumn(headerMap, "CCCD", "CMND", "SỐ CCCD", "MÃ ĐỊNH DANH", "SOCCCD");
                if (cccdCol < 0) continue;

                // Check for "Long Format" (subject-oriented)
                int maMonCol = findColumn(headerMap, "MAMONTHI", "MA_MON", "MÃ MÔN", "MÃ MÔN NN");
                int tenMonCol = findColumn(headerMap, "TENMONTHI", "TEN_MON", "TÊN MÔN");
                int diemCol = findColumn(headerMap, "DIEM", "DIEM_THI", "ĐIỂM", "NN");
                boolean isLongFormat = (maMonCol >= 0 || tenMonCol >= 0) && diemCol >= 0;

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    
                    String cccd = getString(row, cccdCol);
                    if (cccd == null || cccd.isEmpty()) continue;
                    
                    totalRows++;
                    try {
                        String method = loaiDiem;
                        // Auto-detect method from sheet name if loaiDiem is not specifically set
                        if ("Tất cả".equalsIgnoreCase(method)) {
                            String sheetName = sheet.getSheetName().toUpperCase();
                            if (sheetName.contains("VSAT")) method = "VSAT";
                            else if (sheetName.contains("DGNL")) method = "DGNL";
                            else method = "THPT";
                        }

                        final String finalMethod = method;
                        String key = cccd + "_" + finalMethod;
                        Candidate candidate = consolidatedCandidates.computeIfAbsent(cccd, k -> {
                            Candidate c = new Candidate();
                            c.setCccd(cccd);
                            c.setNamTuyenSinh(2025);
                            return c;
                        });

                        CandidateScore score = consolidatedScores.computeIfAbsent(key, k -> {
                            CandidateScore cs = new CandidateScore();
                            cs.setCccd(cccd);
                            cs.setDPhuongthuc(finalMethod);
                            return cs;
                        });

                        if (isLongFormat) {
                            String mCode = getString(row, maMonCol);
                            String mName = getString(row, tenMonCol);
                            BigDecimal val = getDecimal(row, diemCol);
                            applySubjectScore(score, mCode, mName, val);
                        } else {
                            // Wide format - current standard
                            score.setSobaodanh(getString(row, findColumn(headerMap, "SBD", "SỐ BÁO DANH", "SOBAODANH")));
                            if (score.getSobaodanh() != null) candidate.setSobaodanh(score.getSobaodanh());
                            
                            score.setTo(getDecimal(row, findColumn(headerMap, "TO", "TOAN", "TOÁN", "T", "TOÁN HỌC")));
                            score.setVa(getDecimal(row, findColumn(headerMap, "VA", "VAN", "VĂN", "V", "NGỮ VĂN", "NGUVAN")));
                            score.setLi(getDecimal(row, findColumn(headerMap, "LI", "LY", "LÝ", "L", "VẬT LÝ", "VATLY", "VATLI")));
                            score.setHo(getDecimal(row, findColumn(headerMap, "HO", "HOA", "HÓA", "H", "HÓA HỌC", "HOAHOC")));
                            score.setSi(getDecimal(row, findColumn(headerMap, "SI", "SINH", "S", "SINH HỌC", "SINHHOC")));
                            score.setSu(getDecimal(row, findColumn(headerMap, "SU", "SỬ", "LỊCH SỬ", "LICHSU")));
                            score.setDi(getDecimal(row, findColumn(headerMap, "DI", "ĐỊA", "ĐỊA LÝ", "DIALY", "DIALI")));
                            score.setN1Thi(getDecimal(row, findColumn(headerMap, "N1_THI", "NGOAI_NGU", "ANH", "TIẾNG ANH", "N1", "ENGLISH", "TIENGANH", "NN")));
                            score.setN1Cc(getDecimal(row, findColumn(headerMap, "N1_CC", "IELTS", "TOEIC", "NGOAI_NGU_CC", "CHỨNG CHỈ")));
                            score.setNl1(getDecimal(row, findColumn(headerMap, "NL1", "NĂNG LỰC", "DGNL", "NL", "ĐÁNH GIÁ NĂNG LỰC")));
                            
                            // Add missing subjects
                            score.setNk1(getDecimal(row, findColumn(headerMap, "NK1", "NĂNG KHIẾU 1", "NANGKHIEU1", "NK_1", "N.KHIẾU 1")));
                            score.setNk2(getDecimal(row, findColumn(headerMap, "NK2", "NĂNG KHIẾU 2", "NANGKHIEU2", "NK_2", "N.KHIẾU 2")));
                            score.setNk3(getDecimal(row, findColumn(headerMap, "NK3", "NĂNG KHIẾU 3", "NANGKHIEU3", "NK_3", "N.KHIẾU 3")));
                            score.setNk4(getDecimal(row, findColumn(headerMap, "NK4", "NĂNG KHIẾU 4", "NANGKHIEU4", "NK_4", "N.KHIẾU 4")));
                            score.setKtpl(getDecimal(row, findColumn(headerMap, "KTPL", "GDCD", "GIÁO DỤC CÔNG DÂN", "KINH TẾ PHÁP LUẬT")));
                            score.setTi(getDecimal(row, findColumn(headerMap, "TI", "TIN", "TIN HỌC", "TINHOC")));
                            score.setCncn(getDecimal(row, findColumn(headerMap, "CNCN", "CÔNG NGHỆ CÔNG NGHIỆP", "CONGNGHECONGNGHIEP")));
                            score.setCnnn(getDecimal(row, findColumn(headerMap, "CNNN", "CÔNG NGHỆ NÔNG NGHIỆP", "CONGNGHENONGNGHIEP")));
                        }

                        // Update candidate basic info if present
                        updateCandidateInfo(candidate, row, headerMap);
                        successCount++;
                    } catch (Exception e) {
                        errors.add("Sheet " + sheet.getSheetName() + ", Dòng " + (r + 1) + ": " + e.getMessage());
                    }
                }
            }
        }
        
        candidatesBatch.addAll(consolidatedCandidates.values());
        scoresBatch.addAll(consolidatedScores.values());

        if (!scoresBatch.isEmpty()) {
            candidateRepo.saveAll(candidatesBatch);
            repository.saveAll(scoresBatch);
        }

        return new ImportResult(totalRows, successCount, errors);
    }

    private void applySubjectScore(CandidateScore score, String code, String name, BigDecimal val) {
        if (val == null) return;
        String c = (code != null ? code.trim().toUpperCase() : "");
        String n = (name != null ? name.trim().toUpperCase() : "");
        String combined = c + " " + n;
        
        // 1. DGNL - High priority to avoid substring collisions (e.g., "Đánh" contains "anh")
        if (combined.contains("ĐÁNH GIÁ NĂNG LỰC") || c.contains("DGNL") || c.contains("NL1")) {
            score.setNl1(val);
            return;
        }

        // 2. Specific VSAT Codes
        if (c.equals("M1") || c.equals("TO_VS")) { score.setTo(val); return; }
        if (c.equals("M2") || c.equals("LI_VS")) { score.setLi(val); return; }
        if (c.equals("M3") || c.equals("HO_VS")) { score.setHo(val); return; }
        if (c.equals("M4") || c.equals("SI_VS")) { score.setSi(val); return; }
        if (c.equals("M5") || c.equals("SU_VS")) { score.setSu(val); return; }
        if (c.equals("M6") || c.equals("DI_VS")) { score.setDi(val); return; }
        if (c.equals("M8") || c.equals("N1_VS") || c.startsWith("N1") || c.equals("NN")) { score.setN1Thi(val); return; }
        if (c.equals("VA_VS")) { score.setVa(val); return; }

        // 3. Name fallback - Check specific names to avoid overlaps
        if (n.contains("TOÁN")) score.setTo(val);
        else if (n.contains("VĂN")) score.setVa(val);
        else if (n.contains("ĐỊA")) score.setDi(val); // Check "Địa" before "Lý" because "Địa lý" contains "lý"
        else if (n.contains("LÝ") || n.contains("VẬT LÝ")) score.setLi(val);
        else if (n.contains("HÓA")) score.setHo(val);
        else if (n.contains("SINH")) score.setSi(val);
        else if (n.contains("SỬ")) score.setSu(val);
        else if (n.contains("TIẾNG ANH") || n.contains("NGOẠI NGỮ") || n.contains("ENGLISH") || n.contains("ANH")) score.setN1Thi(val);
        else if (n.contains("KTPL") || n.contains("PHÁP LUẬT") || n.contains("GDCD")) score.setKtpl(val);
        else if (n.contains("TIN")) score.setTi(val);
        else if (n.contains("NĂNG KHIẾU 1") || c.contains("NK1")) score.setNk1(val);
        else if (n.contains("NĂNG KHIẾU 2") || c.contains("NK2")) score.setNk2(val);
        else if (n.contains("NĂNG KHIẾU 3") || c.contains("NK3")) score.setNk3(val);
        else if (n.contains("NĂNG KHIẾU 4") || c.contains("NK4")) score.setNk4(val);
        else if (n.contains("CÔNG NGHỆ CÔNG NGHIỆP") || c.contains("CNCN")) score.setCncn(val);
        else if (n.contains("CÔNG NGHỆ NÔNG NGHIỆP") || c.contains("CNNN")) score.setCnnn(val);
    }

    private void updateCandidateInfo(Candidate c, Row row, Map<String, Integer> hMap) {
        String sbd = getString(row, findColumn(hMap, "SBD", "SỐ BÁO DANH"));
        if (sbd != null) c.setSobaodanh(sbd);

        String hoTen = getString(row, findColumn(hMap, "HO_TEN", "HỌ VÀ TÊN", "HOTEN"));
        if (hoTen != null) {
            String[] parts = hoTen.trim().split("\\s+", 2);
            if (parts.length == 2) { c.setHo(parts[0]); c.setTen(parts[1]); }
            else { c.setHo(hoTen.trim()); c.setTen(""); }
        }
        
        String ns = getString(row, findColumn(hMap, "NGAY_SINH", "NGAYSINH"));
        if (ns != null) c.setNgaySinh(parseDate(ns));
        
        String gt = getString(row, findColumn(hMap, "GIOI_TINH", "GIOITINH"));
        if (gt != null) c.setGioiTinh(gt);
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }


    private int findColumn(Map<String, Integer> headerMap, String... aliases) {
        for (String alias : aliases) {
            Integer col = headerMap.get(alias.toUpperCase());
            if (col != null) return col;
        }
        return -1;
    }

    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                java.time.format.DateTimeFormatter formatter = 
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return java.time.LocalDate.parse(dateStr, formatter);
            } catch (Exception ex) {
                return null;
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

    public List<CandidateScore> getScoresByCccd(String cccd) {
        return repository.findByCccd(cccd);
    }

    // ================= HELPER =================

    private String getString(Row row, int col) {
        if (col < 0) return null;
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
        if (col < 0) return null;
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
                "TO", "VA", "LI", "HO", "SI", "SU", "DI",
                "N1_THI", "N1_CC", "CNCN", "CNNN",
                "TI", "KTPL", "NL1", "NK1", "NK2", "NK3", "NK4"
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

            case "NK3":
                return score.getNk3();

            case "NK4":
                return score.getNk4();

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