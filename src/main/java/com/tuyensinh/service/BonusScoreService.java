package com.tuyensinh.service;

import java.math.BigDecimal;
import java.util.List;

import com.tuyensinh.entity.BonusScore;
import com.tuyensinh.repository.BonusScoreRepository;

public class BonusScoreService {

    private final BonusScoreRepository repository;

    public BonusScoreService() {
        this.repository = new BonusScoreRepository();
    }

    public List<Object[]> getBonusScores(String searchTerm) {
        return repository.findAllWithMajor(searchTerm);
    }

    public BonusScore getById(Integer id) {
        return repository.findById(id);
    }

    public void addBonusScore(BonusScore bonusScore) {
        prepare(bonusScore);
        repository.save(bonusScore);
    }

    public void updateBonusScore(BonusScore bonusScore) {
        if (bonusScore.getId() == null) {
            throw new RuntimeException("Thieu ID diem cong.");
        }
        prepare(bonusScore);
        repository.update(bonusScore);
    }

    public void deleteBonusScore(Integer id) {
        if (id == null) {
            throw new RuntimeException("Vui long chon ban ghi diem cong.");
        }
        repository.delete(id);
    }

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

    public ImportResult importHsgBonus(java.io.File file) throws Exception {
        List<BonusScore> batch = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        // Subject mapping
        java.util.Map<String, String> monMap = new java.util.HashMap<>();
        monMap.put("TO", "Toán"); monMap.put("TOAN", "Toán"); monMap.put("T", "Toán");
        monMap.put("VA", "Văn"); monMap.put("VAN", "Văn"); monMap.put("V", "Văn");
        monMap.put("LI", "Lý"); monMap.put("LY", "Lý"); monMap.put("L", "Lý");
        monMap.put("HO", "Hóa"); monMap.put("HOA", "Hóa"); monMap.put("H", "Hóa");
        monMap.put("SI", "Sinh"); monMap.put("SINH", "Sinh"); monMap.put("S", "Sinh");
        monMap.put("SU", "Sử");
        monMap.put("DI", "Địa");
        monMap.put("AN", "Anh"); monMap.put("ANH", "Anh"); monMap.put("A", "Anh");
        monMap.put("GD", "GDCD"); monMap.put("GDCD", "GDCD");
        monMap.put("KTPL", "KTPL");
        monMap.put("TI", "T.Anh"); monMap.put("T.ANH", "T.Anh");
        monMap.put("N1", "Anh"); // Mapping N1 to Anh as common in these files
        monMap.put("N1_THI", "Anh");
        monMap.put("N1_CC", "Anh");
        monMap.put("CNCN", "CN Công nghiệp");
        monMap.put("CNNN", "CN Nông nghiệp");
        monMap.put("NL1", "N.Lực 1");
        monMap.put("NK1", "N.Khiếu 1");
        monMap.put("NK2", "N.Khiếu 2");
        monMap.put("NK3", "N.Khiếu 3");
        monMap.put("NK4", "N.Khiếu 4");

        // Pre-load data
        com.tuyensinh.repository.AspirationRepository aspRepo = new com.tuyensinh.repository.AspirationRepository();
        List<com.tuyensinh.entity.Aspiration> allAsps = aspRepo.findAll();
        java.util.Map<String, List<com.tuyensinh.entity.Aspiration>> aspsByCccd = new java.util.HashMap<>();
        for (com.tuyensinh.entity.Aspiration a : allAsps) {
            if (a.getCccd() != null) aspsByCccd.computeIfAbsent(a.getCccd().trim(), k -> new java.util.ArrayList<>()).add(a);
        }

        com.tuyensinh.repository.ToHopMonRepository thmRepo = new com.tuyensinh.repository.ToHopMonRepository();
        java.util.Map<String, com.tuyensinh.entity.XtToHopMon> thmMap = new java.util.HashMap<>();
        for (com.tuyensinh.entity.XtToHopMon thm : thmRepo.findAll()) {
            if (thm.getMatohop() != null) thmMap.put(thm.getMatohop().trim().toUpperCase(), thm);
        }

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {
            
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String cccd = getStr(row, 1); // Cột B: CCCD
                if (cccd == null || cccd.isEmpty()) continue;
                
                totalRows++;
                try {
                    String cap = getStr(row, 2); // Cột C: Cấp
                    String monAbbr = getStr(row, 4); // Cột E: Mã môn
                    String loaiGiai = getStr(row, 5); // Cột F: Loại giải
                    BigDecimal bonusWithSubj = getNum(row, 6); // Cột G
                    BigDecimal bonusWithoutSubj = getNum(row, 7); // Cột H
                    
                    String subjName = monMap.getOrDefault(monAbbr == null ? "" : monAbbr.toUpperCase(), monAbbr);
                    List<com.tuyensinh.entity.Aspiration> asps = aspsByCccd.get(cccd);
                    
                    if (asps == null || asps.isEmpty()) {
                        continue;
                    }

                    for (com.tuyensinh.entity.Aspiration asp : asps) {
                        com.tuyensinh.entity.XtToHopMon thm = thmMap.get(asp.getToHop() == null ? "" : asp.getToHop().trim().toUpperCase());
                        boolean hasSubj = false;
                        if (thm != null) {
                            if (isSameSubject(subjName, thm.getMon1()) || 
                                isSameSubject(subjName, thm.getMon2()) || 
                                isSameSubject(subjName, thm.getMon3())) {
                                hasSubj = true;
                            }
                        }
                        
                        BigDecimal bonus = hasSubj ? bonusWithSubj : bonusWithoutSubj;
                        if (bonus == null) bonus = BigDecimal.ZERO;

                        BonusScore bs = new BonusScore();
                        bs.setCccd(cccd);
                        bs.setNganhId(asp.getNganhId());
                        bs.setMaToHop(asp.getToHop());
                        bs.setPhuongThuc(asp.getPhuongThuc());
                        bs.setDiemCc(BigDecimal.ZERO);
                        bs.setDiemUtxt(bonus);
                        bs.setDiemTong(bonus);
                        bs.setGhiChu("HSG " + subjName + " (" + cap + " - " + loaiGiai + ")");
                        bs.setDcKeys(bs.getCccd() + "_" + bs.getNganhId() + "_" + bs.getMaToHop());
                        
                        batch.add(bs);
                    }
                    successCount++;
                } catch (Exception ex) {
                    errors.add("Dòng " + (row.getRowNum() + 1) + ": " + ex.getMessage());
                }
            }
        }
        
        if (!batch.isEmpty()) repository.saveAll(batch);
        return new ImportResult(totalRows, successCount, errors);
    }

    public ImportResult importEnglishBonus(java.io.File file) throws Exception {
        List<BonusScore> batch = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        com.tuyensinh.repository.AspirationRepository aspRepo = new com.tuyensinh.repository.AspirationRepository();
        com.tuyensinh.repository.CandidateScoreRepository scoreRepo = new com.tuyensinh.repository.CandidateScoreRepository();

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {
            
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String cccd = getStr(row, 1); // Cột B: CCCD
                if (cccd == null || cccd.isEmpty()) continue;
                
                totalRows++;
                try {
                    String certificate = getStr(row, 2); // Cột C: Chứng chỉ
                    String scoreLevel = getStr(row, 3); // Cột D: Điểm/Bậc
                    BigDecimal subjectScore = getNum(row, 4); // Cột E: Điểm quy đổi (môn Anh)
                    BigDecimal bonusPoint = getNum(row, 5); // Cột F: Điểm cộng (CC)
                    
                    // 1. Update Candidate Score (n1Cc)
                    List<com.tuyensinh.entity.CandidateScore> scores = scoreRepo.findByCccd(cccd);
                    for (com.tuyensinh.entity.CandidateScore s : scores) {
                        s.setN1Cc(subjectScore);
                        scoreRepo.update(s);
                    }

                    // 2. Update Bonus Scores for all aspirations
                    List<com.tuyensinh.entity.Aspiration> asps = aspRepo.findAllByCccd(cccd);
                    if (asps != null) {
                        for (com.tuyensinh.entity.Aspiration asp : asps) {
                            BonusScore bs = new BonusScore();
                            bs.setCccd(cccd);
                            bs.setNganhId(asp.getNganhId());
                            bs.setMaToHop(asp.getToHop());
                            bs.setPhuongThuc(asp.getPhuongThuc());
                            bs.setDiemCc(bonusPoint);
                            bs.setDiemUtxt(BigDecimal.ZERO);
                            bs.setDiemTong(bonusPoint);
                            bs.setGhiChu("Quy đổi chứng chỉ " + certificate + " (" + scoreLevel + ")");
                            bs.setDcKeys(bs.getCccd() + "_" + bs.getNganhId() + "_" + bs.getMaToHop());
                            
                            batch.add(bs);
                        }
                    }
                    successCount++;
                } catch (Exception ex) {
                    errors.add("Dòng " + (row.getRowNum() + 1) + ": " + ex.getMessage());
                }
            }
        }
        
        if (!batch.isEmpty()) repository.saveAll(batch);
        return new ImportResult(totalRows, successCount, errors);
    }

    private boolean isSameSubject(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        String v1 = s1.trim().toUpperCase();
        String v2 = s2.trim().toUpperCase();
        if (v1.equals(v2)) return true;
        
        // Handle variations
        if (isEnglish(v1) && isEnglish(v2)) return true;
        if (isCivics(v1) && isCivics(v2)) return true;
        if (isTech(v1) && isTech(v2)) return true;
        
        return false;
    }

    private boolean isEnglish(String v) {
        return v.equals("ANH") || v.equals("T.ANH") || v.equals("TIẾNG ANH") || v.equals("TIENG ANH") || v.equals("N1") || v.equals("N1_THI") || v.equals("N1_CC") || v.equals("TI");
    }

    private boolean isCivics(String v) {
        return v.equals("GDCD") || v.equals("KTPL") || v.equals("KINH TẾ PHÁP LUẬT") || v.equals("GIÁO DỤC CÔNG DÂN");
    }

    private boolean isTech(String v) {
        if (v.contains("CN CÔNG NGHIỆP") || v.contains("CNCN")) return true;
        if (v.contains("CN NÔNG NGHIỆP") || v.contains("CNNN")) return true;
        return false;
    }

    private String getStr(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return null;
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return null;
    }

    private BigDecimal getNum(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return BigDecimal.ZERO;
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue());
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            try { return new BigDecimal(cell.getStringCellValue().trim()); } catch (Exception e) { return BigDecimal.ZERO; }
        }
        return BigDecimal.ZERO;
    }

    public void syncEnglishCertificates() {
        com.tuyensinh.repository.EnglishCertificateRepository englishRepo = new com.tuyensinh.repository.EnglishCertificateRepository();
        com.tuyensinh.repository.AspirationRepository aspirationRepo = new com.tuyensinh.repository.AspirationRepository();
        EnglishCertificateService englishService = new EnglishCertificateService();
        
        List<com.tuyensinh.entity.EnglishCertificate> certs = englishRepo.findAll();
        List<com.tuyensinh.entity.Aspiration> aspirations = aspirationRepo.findAll();
        
        for (com.tuyensinh.entity.EnglishCertificate cert : certs) {
            String cccd = cert.getCccd();
            if (cccd == null) continue;
            
            for (com.tuyensinh.entity.Aspiration asp : aspirations) {
                if (cccd.trim().equalsIgnoreCase(asp.getCccd() == null ? "" : asp.getCccd().trim())) {
                    int level = englishService.getLevel(cert.getLoaiCc(), cert.getDiemSo().doubleValue());
                    BigDecimal bonus = englishService.getBonusPoints(level, asp.getPhuongThuc());
                    
                    if (bonus.compareTo(BigDecimal.ZERO) > 0) {
                        BonusScore bs = new BonusScore();
                        bs.setCccd(cccd.trim());
                        bs.setNganhId(asp.getNganhId());
                        bs.setMaToHop(asp.getToHop());
                        bs.setPhuongThuc(asp.getPhuongThuc());
                        bs.setDiemCc(bonus);
                        bs.setDiemUtxt(BigDecimal.ZERO);
                        bs.setDiemTong(bonus);
                        bs.setGhiChu("Tự động đồng bộ từ chứng chỉ " + cert.getLoaiCc());
                        bs.setDcKeys(bs.getCccd() + "_" + bs.getNganhId() + "_" + bs.getMaToHop());
                        
                        try {
                            repository.save(bs);
                        } catch (Exception e) {
                            // Already exists
                        }
                    }
                }
            }
        }
    }

    private void prepare(BonusScore bonusScore) {
        bonusScore.setCccd(required(bonusScore.getCccd(), "CCCD"));
        if (bonusScore.getNganhId() == null) {
            throw new RuntimeException("Ma nganh khong duoc de trong.");
        }
        bonusScore.setMaToHop(required(bonusScore.getMaToHop(), "Ma to hop"));
        bonusScore.setPhuongThuc(clean(bonusScore.getPhuongThuc()));
        bonusScore.setGhiChu(clean(bonusScore.getGhiChu()));

        BigDecimal diemCc = zeroIfNull(bonusScore.getDiemCc());
        BigDecimal diemUtxt = zeroIfNull(bonusScore.getDiemUtxt());
        
        // Always recalculate total
        bonusScore.setDiemTong(diemCc.add(diemUtxt));

        bonusScore.setDcKeys(
                bonusScore.getCccd() + "_" + bonusScore.getNganhId() + "_" + bonusScore.getMaToHop());
    }

    private String required(String value, String fieldName) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new RuntimeException(fieldName + " khong duoc de trong.");
        }
        return cleaned;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
