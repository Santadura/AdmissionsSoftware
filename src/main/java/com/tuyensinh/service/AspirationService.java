package com.tuyensinh.service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tuyensinh.entity.Aspiration;
import com.tuyensinh.repository.AspirationRepository;
import com.tuyensinh.repository.BonusScoreRepository;

public class AspirationService {

    private final AspirationRepository repository;
    private final BonusScoreRepository bonusRepository;
    private final com.tuyensinh.repository.CandidateRepository candidateRepository;
    private final EnglishCertificateService englishService;
    private final com.tuyensinh.repository.EnglishCertificateRepository englishRepo;

    public AspirationService() {
        this.repository = new AspirationRepository();
        this.bonusRepository = new BonusScoreRepository();
        this.candidateRepository = new com.tuyensinh.repository.CandidateRepository();
        this.englishService = new EnglishCertificateService();
        this.englishRepo = new com.tuyensinh.repository.EnglishCertificateRepository();
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

    public ImportResult importExcel(File file) throws Exception {
        List<Aspiration> batch = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        // Pre-load data for efficiency
        Map<String, Integer> nganhMap = new HashMap<>();
        for (com.tuyensinh.entity.XtNganh n : new com.tuyensinh.service.NganhService().getAll()) {
            nganhMap.put(n.getManganh().trim().toUpperCase(), n.getIdnganh());
        }

        List<com.tuyensinh.entity.CandidateScore> allScores = new com.tuyensinh.repository.CandidateScoreRepository().findAll();
        Map<String, List<com.tuyensinh.entity.CandidateScore>> scoresByCccd = new HashMap<>();
        for (com.tuyensinh.entity.CandidateScore s : allScores) {
            if (s.getCccd() != null) scoresByCccd.computeIfAbsent(s.getCccd().trim(), k -> new ArrayList<>()).add(s);
        }

        List<com.tuyensinh.entity.MajorCombination> allMajorCombinations = new com.tuyensinh.repository.MajorCombinationRepository().findAll();
        Map<Integer, List<com.tuyensinh.entity.MajorCombination>> majorToCombs = new HashMap<>();
        for (com.tuyensinh.entity.MajorCombination mc : allMajorCombinations) {
            majorToCombs.computeIfAbsent(mc.getNganhId(), k -> new ArrayList<>()).add(mc);
        }

        List<com.tuyensinh.entity.XtToHopMon> allToHopMon = new com.tuyensinh.repository.ToHopMonRepository().findAll();
        Map<String, com.tuyensinh.entity.XtToHopMon> toHopMap = new HashMap<>();
        for (com.tuyensinh.entity.XtToHopMon thm : allToHopMon) {
            if (thm.getMatohop() != null) toHopMap.put(thm.getMatohop().trim().toUpperCase(), thm);
        }

        List<com.tuyensinh.entity.ScoreConversion> allConversions = new com.tuyensinh.repository.ScoreConversionRepository().findAll();
        Map<String, List<com.tuyensinh.entity.ScoreConversion>> conversionMap = new HashMap<>();
        for (com.tuyensinh.entity.ScoreConversion sc : allConversions) {
            String key = (sc.getPhuongThuc() == null ? "" : sc.getPhuongThuc().trim().toUpperCase()) + "_" 
                       + (sc.getMon() == null ? "" : sc.getMon().trim().toUpperCase());
            conversionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(sc);
        }

        Map<String, com.tuyensinh.entity.EnglishCertificate> certMap = englishRepo.findAllAsMap();
        Map<Integer, String> rootCombs = repository.findMajorRootCombinations();
        
        // Load existing aspirations to avoid duplicates - Match by CCCD and Order (The most stable ID)
        List<Aspiration> existingAsps = repository.findAll();
        Map<String, Aspiration> existingMap = new HashMap<>();
        for (Aspiration a : existingAsps) {
            String key = a.getCccd() + "_" + (a.getThuTu() != null ? a.getThuTu() : "0");
            existingMap.put(key, a);
        }
        Set<String> processedInThisImport = new HashSet<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet.getPhysicalNumberOfRows() == 0) continue;

                String sheetName = sheet.getSheetName().toUpperCase();
                String defaultMethodFromSheet = null;
                if (sheetName.contains("VSAT") || sheetName.contains("V-SAT")) defaultMethodFromSheet = "VSAT";
                else if (sheetName.contains("DGNL") || sheetName.contains("ĐGNL")) defaultMethodFromSheet = "DGNL";
                else if (sheetName.contains("THPT")) defaultMethodFromSheet = "THPT";

                // 1. Find header row (scan first 15 rows for more flexibility)
                Row headerRow = null;
                Map<String, Integer> hMap = new HashMap<>();
                List<String> detectedHeaders = new ArrayList<>();
                
                for (int r = 0; r <= Math.min(sheet.getLastRowNum(), 15); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    hMap.clear();
                    detectedHeaders.clear();
                    for (Cell cell : row) {
                        String val = getCellString(cell);
                        if (val != null && !val.isEmpty()) {
                            String upperVal = val.trim().toUpperCase();
                            hMap.put(upperVal, cell.getColumnIndex());
                            detectedHeaders.add(upperVal);
                        }
                    }
                    
                    // Check if this row looks like a header row
                    boolean hasCccd = false;
                    boolean hasMaXT = false;
                    for (String header : hMap.keySet()) {
                        if (header.contains("CCCD") || header.contains("CMND")) hasCccd = true;
                        if (header.contains("MÃ XÉT TUYỂN") || header.contains("MA_XET_TUYEN") || 
                            header.contains("MÃ NGÀNH") || header.contains("MANGANH")) hasMaXT = true;
                    }
                    
                    if (hasCccd && hasMaXT) {
                        headerRow = row;
                        break;
                    }
                }

                if (headerRow == null) {
                    errors.add("Sheet '" + sheet.getSheetName() + "': Không tự động tìm thấy dòng tiêu đề. Vui lòng đảm bảo có cột 'CCCD' và 'Mã xét tuyển'.");
                    continue;
                }

                // 2. Map columns using fuzzy matching
                int cccdCol = -1, thuTuCol = -1, maNganhCol = -1, tuyenThangCol = -1, toHopCol = -1, phuongThucCol = -1;
                for (Map.Entry<String, Integer> entry : hMap.entrySet()) {
                    String k = entry.getKey();
                    int v = entry.getValue();
                    if (cccdCol < 0 && (k.contains("CCCD") || k.contains("CMND"))) cccdCol = v;
                    if (thuTuCol < 0 && (k.contains("THỨ TỰ") || k.contains("THUTU") || k.contains("NV_TT"))) thuTuCol = v;
                    if (maNganhCol < 0 && (k.contains("MÃ XÉT TUYỂN") || k.contains("MÃ NGÀNH") || k.contains("MANGANH") || k.contains("MA_XET_TUYEN"))) maNganhCol = v;
                    if (tuyenThangCol < 0 && (k.contains("TUYỂN THẲNG") || k.contains("ĐIỀU 8") || k.contains("DIEU 8"))) tuyenThangCol = v;
                    if (toHopCol < 0 && (k.contains("TỔ HỢP") || k.contains("TOHOP") || k.contains("MA_TO_HOP"))) toHopCol = v;
                    if (phuongThucCol < 0 && (k.contains("PHƯƠNG THỨC") || k.contains("PHUONGTHUC") || k.contains("PTXT"))) phuongThucCol = v;
                }

                if (cccdCol < 0 || maNganhCol < 0) {
                    errors.add("Sheet '" + sheet.getSheetName() + "': Thiếu cột bắt buộc. Cột tìm thấy: " + String.join(", ", detectedHeaders));
                    continue;
                }

                for (int r = headerRow.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    
                    String cccd = getStr(row, cccdCol);
                    String maNganh = getStr(row, maNganhCol);
                    String toHopExcel = getStr(row, toHopCol);
                    String phuongThucExcel = getStr(row, phuongThucCol);
                    if (cccd == null || cccd.isEmpty()) continue;
                    
                    totalRows++;

                    try {
                        if (maNganh == null || maNganh.isEmpty()) {
                            errors.add("Dòng " + (r + 1) + " sheet '" + sheet.getSheetName() + "': Mã ngành bị trống.");
                            continue;
                        }
                        
                        // Clean maNganh (in case it contains combination or school code)
                        String cleanMaNganh = maNganh.trim().toUpperCase();
                        if (cleanMaNganh.contains("-")) cleanMaNganh = cleanMaNganh.split("-")[0].trim();
                        if (cleanMaNganh.contains(".")) cleanMaNganh = cleanMaNganh.split("\\.")[0].trim();

                        Integer nganhId = nganhMap.get(cleanMaNganh);
                        if (nganhId == null) {
                            errors.add("Dòng " + (r + 1) + " sheet '" + sheet.getSheetName() + "': Mã ngành '" + maNganh + "' không tồn tại trong hệ thống.");
                            continue;
                        }

                        Aspiration asp = new Aspiration();
                        asp.setCccd(cccd);
                        asp.setNganhId(nganhId);
                        asp.setThuTu(parseVal(getStr(row, thuTuCol), 1));
                        
                        // Set Phuong Thuc from Excel if available
                        if (phuongThucExcel != null && !phuongThucExcel.trim().isEmpty()) {
                            String pt = phuongThucExcel.trim().toUpperCase();
                            if (pt.contains("VSAT") || pt.contains("V-SAT")) asp.setPhuongThuc("VSAT");
                            else if (pt.contains("DGNL") || pt.contains("ĐGNL")) asp.setPhuongThuc("DGNL");
                            else if (pt.contains("THPT")) asp.setPhuongThuc("THPT");
                            else asp.setPhuongThuc(pt); // Keep original if doesn't match standard
                        }
                        
                        String tuyenThangStr = getStr(row, tuyenThangCol);
                        boolean isTuyenThang = tuyenThangStr != null && !tuyenThangStr.trim().isEmpty() 
                                            && !tuyenThangStr.equalsIgnoreCase("không")
                                            && !tuyenThangStr.equals("0");

                        // 1. Try to extract combination from Excel directly or from the maNganh string itself
                        String extractedComb = (toHopExcel != null && !toHopExcel.trim().isEmpty()) ? toHopExcel.trim().toUpperCase() : null;
                        if (extractedComb == null) {
                            for (String thCode : toHopMap.keySet()) {
                                if (maNganh.toUpperCase().contains(thCode)) {
                                    extractedComb = thCode;
                                    break;
                                }
                            }
                        }

                        if (isTuyenThang) {
                            asp.setKetQua("trungtuyen");
                            asp.setPhuongThuc("XTT");
                            
                            // For direct admission, use extracted comb or root comb or first allowed or A00
                            String finalComb = extractedComb;
                            if (finalComb == null || finalComb.isEmpty()) {
                                finalComb = rootCombs.get(nganhId);
                            }
                            if (finalComb == null || finalComb.isEmpty()) {
                                List<com.tuyensinh.entity.MajorCombination> allowed = majorToCombs.get(nganhId);
                                if (allowed != null && !allowed.isEmpty()) finalComb = allowed.get(0).getMaToHop();
                            }
                            if (finalComb == null || finalComb.isEmpty()) {
                                finalComb = "A00";
                            }
                            asp.setToHop(finalComb);
                        } else {
                            asp.setKetQua("chuaxet");
                            com.tuyensinh.entity.EnglishCertificate cert = certMap.get(cccd);
                            BigDecimal certEnglishScore = null;
                            if (cert != null) {
                                int level = englishService.getLevel(cert.getLoaiCc(), cert.getDiemSo().doubleValue());
                                certEnglishScore = englishService.getEnglishSubjectScore(level);
                            }

                            // CRITICAL: Do NOT set asp.setToHop yet, so findBestScore searches across ALL allowed combinations
                            BestScoreResult best = findBestScore(asp, scoresByCccd, majorToCombs, toHopMap, conversionMap, certEnglishScore);
                            
                            if (best != null) {
                                asp.setToHop(best.combination);
                                
                                // Only override method if it wasn't explicitly set in the Excel file
                                if (asp.getPhuongThuc() == null) {
                                    asp.setPhuongThuc(best.method);
                                }
                            } else {
                                // No best score found, use multi-tier fallback
                                String fallback = extractedComb;
                                if (fallback == null || fallback.isEmpty()) {
                                    fallback = rootCombs.get(nganhId);
                                }
                                if (fallback == null || fallback.isEmpty()) {
                                    List<com.tuyensinh.entity.MajorCombination> allowed = majorToCombs.get(nganhId);
                                    if (allowed != null && !allowed.isEmpty()) {
                                        fallback = allowed.get(0).getMaToHop();
                                    }
                                }
                                if (fallback == null || fallback.isEmpty()) {
                                    fallback = "A00";
                                }
                                asp.setToHop(fallback);
                            }
                        }

                        // Final check to prevent empty combination and synchronize fields
                        if (asp.getToHop() == null || asp.getToHop().trim().isEmpty()) {
                             String root = rootCombs.get(nganhId);
                             asp.setToHop((root != null && !root.trim().isEmpty()) ? root.trim() : "A00"); 
                        }
                        asp.setTtThm(asp.getToHop());


                        if (asp.getPhuongThuc() == null) asp.setPhuongThuc("THPT");
                        String currentNvKey = asp.getCccd() + "_" + asp.getThuTu() + "_" + asp.getNganhId() + "_" + keyPart(asp.getPhuongThuc()) + "_" + keyPart(asp.getToHop());
                        asp.setNvKeys(currentNvKey);

                        // DEDUP: Skip if already processed in this file, or update if exists in DB
                        if (processedInThisImport.contains(currentNvKey)) continue;
                        processedInThisImport.add(currentNvKey);

                        String lookupKey = asp.getCccd() + "_" + asp.getThuTu();
                        Aspiration existing = existingMap.get(lookupKey);
                        if (existing != null) {
                            asp.setId(existing.getId()); // Set ID to trigger UPDATE instead of INSERT
                        }

                        batch.add(asp);
                        successCount++;

                        if (batch.size() >= 1000) {
                            repository.saveAll(batch);
                            batch.clear();
                        }
                    } catch (Exception ex) {
                        errors.add("Dòng " + (r + 1) + " sheet '" + sheet.getSheetName() + "': " + ex.getMessage());
                    }
                }
            }
        }


        if (!batch.isEmpty()) repository.saveAll(batch);
        return new ImportResult(totalRows, successCount, errors);
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        if (cell.getCellType() == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        return null;
    }

    private int findCol(Map<String, Integer> hMap, String... names) {
        for (String n : names) if (hMap.containsKey(n.toUpperCase())) return hMap.get(n.toUpperCase());
        return -1;
    }

    private String getStr(Row row, int col) {
        if (col < 0) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return null;
    }

    private Integer parseVal(String s, int def) {
        try { return Integer.valueOf(s); } catch (Exception e) { return def; }
    }

    public List<Object[]> getAspirationsWithCandidate(String searchTerm) {
        return repository.findAllWithCandidate(searchTerm);
    }

    public List<Aspiration> getAspirations(String searchTerm) {
        return repository.findAll();
    }

    public Aspiration getById(Integer id) {
        return repository.findById(id);
    }

    public void addAspiration(Aspiration aspiration) {
        prepare(aspiration);
        repository.save(aspiration);
    }

    public void updateAspiration(Aspiration aspiration) {
        if (aspiration.getId() == null) {
            throw new RuntimeException("Thieu ID nguyen vong.");
        }
        prepare(aspiration);
        repository.update(aspiration);
    }

    public void deleteAspiration(Integer id) {
        if (id == null) {
            throw new RuntimeException("Vui long chon nguyen vong.");
        }
        repository.delete(id);
    }

    public AdmissionResult runAdmission() {
        return calculateAdmission(true);
    }

    public AdmissionResult previewAdmission() {
        return calculateAdmission(false);
    }

    private AdmissionResult calculateAdmission(boolean saveResult) {
        List<Aspiration> aspirations = repository.findAll();
        Map<Integer, BigDecimal[]> floors = repository.findMajorFloors();
        Map<Integer, Integer> quotas = repository.findMajorQuotas();
        Map<Integer, String> rootCombinations = repository.findMajorRootCombinations();
        
        // Detailed bonus data mapping for fine-grained resolution
        Map<String, List<com.tuyensinh.entity.BonusScore>> allBonusScores = new HashMap<>();
        List<com.tuyensinh.entity.BonusScore> allBonuses = new com.tuyensinh.repository.BonusScoreRepository().findAllWithMajor("").stream()
                .map(row -> (com.tuyensinh.entity.BonusScore) row[0]).toList();
        for (com.tuyensinh.entity.BonusScore bs : allBonuses) {
            allBonusScores.computeIfAbsent(bs.getCccd(), k -> new ArrayList<>()).add(bs);
        }

        Map<String, com.tuyensinh.entity.EnglishCertificate> certMap = englishRepo.findAllAsMap();
        
        // Load data for best combination calculation
        List<com.tuyensinh.entity.CandidateScore> allScores = new com.tuyensinh.repository.CandidateScoreRepository().findAll();
        List<com.tuyensinh.entity.MajorCombination> allMajorCombinations = new com.tuyensinh.repository.MajorCombinationRepository().findAll();
        List<com.tuyensinh.entity.XtToHopMon> allToHopMon = new com.tuyensinh.repository.ToHopMonRepository().findAll();
        
        Map<String, List<com.tuyensinh.entity.CandidateScore>> scoresByCccd = new HashMap<>();
        for (com.tuyensinh.entity.CandidateScore s : allScores) {
            if (s.getCccd() != null) {
                scoresByCccd.computeIfAbsent(s.getCccd().trim(), k -> new ArrayList<>()).add(s);
            }
        }
        
        Map<Integer, List<com.tuyensinh.entity.MajorCombination>> majorToCombs = new HashMap<>();
        for (com.tuyensinh.entity.MajorCombination mc : allMajorCombinations) {
            majorToCombs.computeIfAbsent(mc.getNganhId(), k -> new ArrayList<>()).add(mc);
        }
        
        Map<String, com.tuyensinh.entity.XtToHopMon> toHopMap = new HashMap<>();
        for (com.tuyensinh.entity.XtToHopMon thm : allToHopMon) {
            if (thm.getMatohop() != null) {
                toHopMap.put(thm.getMatohop().trim().toUpperCase(), thm);
            }
        }

        // Load conversions and group by (method + mon)
        List<com.tuyensinh.entity.ScoreConversion> allConversions = new com.tuyensinh.repository.ScoreConversionRepository().findAll();
        Map<String, List<com.tuyensinh.entity.ScoreConversion>> conversionMap = new HashMap<>();
        for (com.tuyensinh.entity.ScoreConversion sc : allConversions) {
            String methodKey = (sc.getPhuongThuc() == null ? "" : sc.getPhuongThuc().trim().toUpperCase());
            String monKey = (sc.getMon() == null ? "" : sc.getMon().trim().toUpperCase());
            String key = methodKey + "_" + monKey;
            conversionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(sc);
        }

        List<Aspiration> eligibleAspirations = new ArrayList<>();

        int belowFloor = 0;
        int missingScore = 0;
        int missingMajorConfig = 0;

        for (Aspiration aspiration : aspirations) {
            
            // 0. Reset scores
            aspiration.setDiemThxt(null);
            aspiration.setDiemCc(BigDecimal.ZERO);
            aspiration.setDiemCong(BigDecimal.ZERO);
            aspiration.setDiemUtqd(BigDecimal.ZERO);
            aspiration.setDiemUtxt(BigDecimal.ZERO);
            aspiration.setDiemXetTuyen(null);

            String currentCccd = aspiration.getCccd() == null ? "" : aspiration.getCccd().trim();

            // 0. Process English Certificate Auto-Conversion
            com.tuyensinh.entity.EnglishCertificate cert = certMap.get(currentCccd);
            int certLevel = 0;
            BigDecimal certBonus = BigDecimal.ZERO;
            BigDecimal certEnglishScore = null;

            if (cert != null) {
                certLevel = englishService.getLevel(cert.getLoaiCc(), cert.getDiemSo().doubleValue());
                certBonus = englishService.getBonusPoints(certLevel, aspiration.getPhuongThuc());
                certEnglishScore = englishService.getEnglishSubjectScore(certLevel);
            }

            // 1. Find best combination and base score (ĐTHXT)
            BestScoreResult best = findBestScore(aspiration, scoresByCccd, majorToCombs, toHopMap, conversionMap, certEnglishScore);
            if (best != null) {
                BigDecimal dthxt = best.score;
                aspiration.setToHop(best.combination);
                
                // 2. Calculate Root Score (ĐTHGXT)
                BigDecimal dthgxt;
                if ("DGNL".equalsIgnoreCase(aspiration.getPhuongThuc())) {
                    dthgxt = dthxt;
                } else {
                    String rootComb = rootCombinations.get(aspiration.getNganhId());
                    BigDecimal deviation = getDeviation(aspiration.getToHop(), rootComb);
                    dthgxt = dthxt.subtract(deviation);
                }
                aspiration.setDiemThxt(dthgxt); 
            }

            // 3. Resolve HSG Bonus (ĐC_HSG) and Raw Priority (MĐƯT) from Candidate Entity
            List<com.tuyensinh.entity.BonusScore> relevantBonuses = allBonusScores.getOrDefault(currentCccd, new ArrayList<>());
            BigDecimal manualHsgBonus = BigDecimal.ZERO;
            BigDecimal syncedCertBonus = BigDecimal.ZERO;

            for (com.tuyensinh.entity.BonusScore bs : relevantBonuses) {
                if (bs.getNganhId() != null && !bs.getNganhId().equals(aspiration.getNganhId())) continue;
                if (hasText(bs.getMaToHop()) && !bs.getMaToHop().equalsIgnoreCase(aspiration.getToHop())) continue;
                if (hasText(bs.getPhuongThuc()) && !bs.getPhuongThuc().equalsIgnoreCase(aspiration.getPhuongThuc())) continue;

                if (bs.getGhiChu() != null && bs.getGhiChu().contains("chứng chỉ")) {
                    syncedCertBonus = syncedCertBonus.add(bs.getDiemCc());
                } else {
                    // Manual bonus from BonusScore is now treated as HSG/Other bonus
                    manualHsgBonus = manualHsgBonus.add(bs.getDiemUtxt());
                }
            }

            // Calculate Area/Object Priority Points (MĐƯT) from Candidate data
            BigDecimal totalMdut = BigDecimal.ZERO;
            com.tuyensinh.entity.Candidate candidate = candidateRepository.findByCccd(currentCccd);
            if (candidate != null) {
                String kv = candidate.getKhuVuc() != null ? candidate.getKhuVuc().trim().toUpperCase() : "";
                String dt = candidate.getDoiTuong() != null ? candidate.getDoiTuong().trim() : "";
                
                // Area priority (KV) - Mapping from bang_du_lieu.md
                if (kv.equals("1") || kv.equals("KV1")) totalMdut = totalMdut.add(new BigDecimal("0.75"));
                else if (kv.equals("2NT") || kv.equals("KV2-NT") || kv.equals("KV2NT")) totalMdut = totalMdut.add(new BigDecimal("0.5"));
                else if (kv.equals("2") || kv.equals("KV2")) totalMdut = totalMdut.add(new BigDecimal("0.25"));
                
                // Object priority (DT) - Clean suffix (e.g., 06a -> 06) and mapping from bang_du_lieu.md
                if (!dt.isEmpty()) {
                    String cleanDt = dt.replaceAll("[^0-9]", ""); // Remove non-numeric characters (suffixes)
                    if (cleanDt.matches("0[1-5]")) totalMdut = totalMdut.add(new BigDecimal("2.0"));
                    else if (cleanDt.matches("0[6-7]")) totalMdut = totalMdut.add(new BigDecimal("1.0"));
                }
            }

            BigDecimal finalCertBonus = syncedCertBonus.max(certBonus);
            BigDecimal totalDc = manualHsgBonus.add(finalCertBonus);
            if (totalDc.compareTo(BigDecimal.valueOf(3)) > 0) totalDc = BigDecimal.valueOf(3);
            
            aspiration.setDiemCc(finalCertBonus);       
            aspiration.setDiemUtxt(manualHsgBonus); 
            aspiration.setDiemCong(totalDc);

            if (aspiration.getDiemThxt() == null) {
                aspiration.setKetQua("chuaxet");
                missingScore++;
                continue;
            }

            // 4. Calculate Scaled Priority (ĐƯT) - According to cac_cong_thuc_tinh.md 3.4
            // baseForPriority = ĐTHGXT + ĐC
            BigDecimal baseForPriority = aspiration.getDiemThxt().add(totalDc);
            BigDecimal dut = calculateScaledPriority(baseForPriority, totalMdut);
            aspiration.setDiemUtqd(dut);    

            // 5. Calculate Final Score (ĐXT) - ĐXT = ĐTHGXT + ĐC + ĐƯT
            aspiration.setDiemXetTuyen(baseForPriority.add(dut));

            if (!floors.containsKey(aspiration.getNganhId()) || !quotas.containsKey(aspiration.getNganhId())) {
                aspiration.setKetQua("chuacauhinh");
                missingMajorConfig++;
                continue;
            }

            BigDecimal floor = floors.get(aspiration.getNganhId())[0];
            BigDecimal manualCutoff = floors.get(aspiration.getNganhId())[1];

            if (aspiration.getDiemXetTuyen().compareTo(floor) < 0) {
                aspiration.setKetQua("duoisan");
                belowFloor++;
                continue;
            }

            if (manualCutoff != null && manualCutoff.compareTo(BigDecimal.ZERO) > 0) {
                if (aspiration.getDiemXetTuyen().compareTo(manualCutoff) < 0) {
                    aspiration.setKetQua("khongtrungtuyen");
                    continue;
                }
            }

            eligibleAspirations.add(aspiration);
        }

        Comparator<Aspiration> ranking = Comparator
                .comparing(Aspiration::getDiemXetTuyen, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(a -> a.getThuTu() == null ? Integer.MAX_VALUE : a.getThuTu())
                .thenComparing(a -> a.getId() == null ? Integer.MAX_VALUE : a.getId());

        int passed = 0;
        Map<String, List<Aspiration>> candidateAspirations = new HashMap<>();
        for (Aspiration aspiration : eligibleAspirations) {
            candidateAspirations.computeIfAbsent(aspiration.getCccd(), k -> new ArrayList<>()).add(aspiration);
        }

        for (List<Aspiration> asps : candidateAspirations.values()) {
            asps.sort(Comparator.comparing(a -> a.getThuTu() == null ? Integer.MAX_VALUE : a.getThuTu()));
        }

        java.util.Queue<String> applicantsToCheck = new java.util.LinkedList<>(candidateAspirations.keySet());
        Map<String, Integer> candidateNextAspIndex = new HashMap<>();
        for (String cccd : candidateAspirations.keySet()) {
            candidateNextAspIndex.put(cccd, 0);
        }

        // OPTIMIZED: Use PriorityQueue (Min-Heap of rank) to maintain top candidates for each major
        // The "smallest" element in this PQ will be the "worst" candidate (lowest score, highest priority number)
        Comparator<Aspiration> antiRanking = ranking.reversed();
        Map<Integer, PriorityQueue<Aspiration>> currentAdmitted = new HashMap<>();
        for (Integer major : quotas.keySet()) {
            currentAdmitted.put(major, new PriorityQueue<>(antiRanking));
        }

        while (!applicantsToCheck.isEmpty()) {
            String cccd = applicantsToCheck.poll();
            int attemptIdx = candidateNextAspIndex.get(cccd);
            List<Aspiration> asps = candidateAspirations.get(cccd);

            if (attemptIdx >= asps.size()) continue;

            Aspiration proposing = asps.get(attemptIdx);
            Integer major = proposing.getNganhId();

            PriorityQueue<Aspiration> admittedInMajor = currentAdmitted.get(major);
            if (admittedInMajor == null) {
                admittedInMajor = new PriorityQueue<>(antiRanking);
                currentAdmitted.put(major, admittedInMajor);
            }
            
            admittedInMajor.add(proposing);

            int quota = quotas.getOrDefault(major, 0);
            if (admittedInMajor.size() > quota) {
                Aspiration rejected = admittedInMajor.poll(); // Removes the "worst" candidate efficiently
                String rejectedCccd = rejected.getCccd();
                candidateNextAspIndex.put(rejectedCccd, candidateNextAspIndex.get(rejectedCccd) + 1);
                applicantsToCheck.add(rejectedCccd);
            }
        }

        int failed = 0;
        for (Aspiration aspiration : eligibleAspirations) {
            aspiration.setKetQua("khongtrungtuyen");
            failed++;
        }
        
        for (PriorityQueue<Aspiration> admitted : currentAdmitted.values()) {
            for (Aspiration aspiration : admitted) {
                aspiration.setKetQua("trungtuyen");
                passed++;
                failed--; 
            }
        }

        if (saveResult) {
            repository.saveAll(aspirations);
        }
        return new AdmissionResult(aspirations.size(), passed, failed, belowFloor, missingScore, missingMajorConfig);
    }

    private boolean isEnglishSubject(String mon) {
        if (mon == null) return false;
        String m = normalizeSubject(mon);
        return m.equals("an") || m.equals("anh") || m.equals("tanh") || m.equals("tienganh")
                || m.equals("n1") || m.equals("n1thi") || m.equals("n1cc") || m.equals("ngoaingu")
                || m.equals("m8") || m.equals("n1vs");
    }
    private BestScoreResult findBestScore(
            Aspiration aspiration,
            Map<String, List<com.tuyensinh.entity.CandidateScore>> scoresByCccd,
            Map<Integer, List<com.tuyensinh.entity.MajorCombination>> majorToCombs,
            Map<String, com.tuyensinh.entity.XtToHopMon> toHopMap,
            Map<String, List<com.tuyensinh.entity.ScoreConversion>> conversionMap,
            BigDecimal certEnglishScore) {
        
        String cccd = aspiration.getCccd() == null ? "" : aspiration.getCccd().trim();
        List<com.tuyensinh.entity.CandidateScore> scores = scoresByCccd.get(cccd);
        if (scores == null) return null;
        
        List<com.tuyensinh.entity.MajorCombination> allowedCombs = majorToCombs.get(aspiration.getNganhId());
        if (allowedCombs == null) return null;
        
        BigDecimal bestScore = null;
        String bestComb = null;
        String bestMethod = null;
        
        for (com.tuyensinh.entity.CandidateScore s : scores) {
            String method = s.getDPhuongthuc() == null ? "" : s.getDPhuongthuc().trim();
            if (aspiration.getPhuongThuc() != null && !aspiration.getPhuongThuc().isEmpty()) {
                if (!aspiration.getPhuongThuc().equalsIgnoreCase(method)) continue;
            }
            
            // Special handling for DGNL: It uses a total score (NL1) instead of individual subjects
            if ("DGNL".equalsIgnoreCase(method)) {
                BigDecimal totalDgnlScore = s.getNl1();
                if (totalDgnlScore != null) {
                    List<com.tuyensinh.entity.ScoreConversion> configs = conversionMap.get("DGNL_");
                    BigDecimal converted = interpolateScore(totalDgnlScore, configs);
                    
                    if (bestScore == null || converted.compareTo(bestScore) > 0) {
                        bestScore = converted.setScale(2, RoundingMode.HALF_UP);
                        // DGNL doesn't strictly have a combination, but we need one for consistency.
                        // We use the first allowed combination, or "DGNL" as a placeholder.
                        bestComb = allowedCombs.isEmpty() ? "DGNL" : allowedCombs.get(0).getMaToHop();
                        bestMethod = "DGNL";
                    }
                }
                continue; // Skip the subject-by-subject combination loop for DGNL
            }
            
            for (com.tuyensinh.entity.MajorCombination mc : allowedCombs) {
                if (aspiration.getToHop() != null && !aspiration.getToHop().isEmpty()) {
                    if (!aspiration.getToHop().equalsIgnoreCase(mc.getMaToHop())) continue;
                }

                com.tuyensinh.entity.XtToHopMon thm = toHopMap.get(mc.getMaToHop() == null ? "" : mc.getMaToHop().trim().toUpperCase());
                if (thm == null) continue;
                
                BigDecimal sum = calculateWeightedSum(s, thm, mc, conversionMap, certEnglishScore);
                if (sum == null) continue;
                
                if (bestScore == null || sum.compareTo(bestScore) > 0) {
                    bestScore = sum.setScale(2, RoundingMode.HALF_UP);
                    bestComb = mc.getMaToHop();
                    bestMethod = method;
                }
            }
        }
        
        return bestScore == null ? null : new BestScoreResult(bestScore, bestComb, bestMethod);
    }

    private BigDecimal calculateWeightedSum(
            com.tuyensinh.entity.CandidateScore s,
            com.tuyensinh.entity.XtToHopMon thm,
            com.tuyensinh.entity.MajorCombination mc,
            Map<String, List<com.tuyensinh.entity.ScoreConversion>> conversionMap,
            BigDecimal certEnglishScore) {
        
        BigDecimal m1 = getSubjectScore(s, thm.getMon1());
        BigDecimal m2 = getSubjectScore(s, thm.getMon2());
        BigDecimal m3 = getSubjectScore(s, thm.getMon3());

        // Apply English conversion for THPT
        if ("THPT".equalsIgnoreCase(s.getDPhuongthuc()) && certEnglishScore != null) {
            if (isEnglishSubject(thm.getMon1())) m1 = (m1 == null) ? certEnglishScore : m1.max(certEnglishScore);
            if (isEnglishSubject(thm.getMon2())) m2 = (m2 == null) ? certEnglishScore : m2.max(certEnglishScore);
            if (isEnglishSubject(thm.getMon3())) m3 = (m3 == null) ? certEnglishScore : m3.max(certEnglishScore);
        }
        
        if (m1 == null || m2 == null || m3 == null) {
            return null;
        }

        String method = s.getDPhuongthuc();
        if ("VSAT".equalsIgnoreCase(method)) {
            m1 = interpolateScore(m1, conversionMap.get("VSAT_" + thm.getMon1().trim().toUpperCase()));
            m2 = interpolateScore(m2, conversionMap.get("VSAT_" + thm.getMon2().trim().toUpperCase()));
            m3 = interpolateScore(m3, conversionMap.get("VSAT_" + thm.getMon3().trim().toUpperCase()));
        }
        
        int w1 = mc.getHsMon1() == null ? 1 : mc.getHsMon1();
        int w2 = mc.getHsMon2() == null ? 1 : mc.getHsMon2();
        int w3 = mc.getHsMon3() == null ? 1 : mc.getHsMon3();
        int totalW = w1 + w2 + w3;
        
        BigDecimal weightedSum = m1.multiply(BigDecimal.valueOf(w1))
                .add(m2.multiply(BigDecimal.valueOf(w2)))
                .add(m3.multiply(BigDecimal.valueOf(w3)));
        
        BigDecimal finalScore = weightedSum.multiply(BigDecimal.valueOf(3))
                .divide(BigDecimal.valueOf(totalW), 5, RoundingMode.HALF_UP);
        
        return finalScore;
    }

    private BigDecimal interpolateScore(BigDecimal x, List<com.tuyensinh.entity.ScoreConversion> configs) {
        if (x == null || configs == null || configs.isEmpty()) return x;
        
        for (com.tuyensinh.entity.ScoreConversion sc : configs) {
            if (sc.getDiemA() == null || sc.getDiemB() == null || sc.getDiemC() == null || sc.getDiemD() == null) continue;
            
            BigDecimal a = BigDecimal.valueOf(sc.getDiemA());
            BigDecimal b = BigDecimal.valueOf(sc.getDiemB());
            
            if (x.compareTo(a) >= 0 && x.compareTo(b) <= 0) {
                BigDecimal c = BigDecimal.valueOf(sc.getDiemC());
                BigDecimal d = BigDecimal.valueOf(sc.getDiemD());
                
                if (b.compareTo(a) == 0) return c;
                
                return c.add(
                    x.subtract(a)
                     .multiply(d.subtract(c))
                     .divide(b.subtract(a), 5, RoundingMode.HALF_UP)
                );
            }
        }
        return x; 
    }

    private String normalizeSubject(String s) {
        if (s == null) return "";
        String n = s.trim().toLowerCase();
        // 1. Remove accents
        n = java.text.Normalizer.normalize(n, java.text.Normalizer.Form.NFD);
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.replace("đ", "d");
        // 2. Remove all non-alphanumeric (removes spaces, dots, dashes)
        n = n.replaceAll("[^a-z0-9]", "");
        return n;
    }

    private BigDecimal getSubjectScore(com.tuyensinh.entity.CandidateScore s, String mon) {
        if (mon == null) return null;
        String m = normalizeSubject(mon);
        
        BigDecimal score = null;
        switch (m) {
            case "t": case "to": case "toan": case "toanhoc": case "m1": case "tovs":
                score = s.getTo(); break;
                
            case "l": case "li": case "ly": case "vatly": case "vatli": case "m2": case "livs":
                score = s.getLi(); break;
                
            case "h": case "ho": case "hoa": case "hoahoc": case "m3": case "hovs":
                score = s.getHo(); break;
                
            case "s": case "si": case "sinh": case "sinhhoc": case "m4": case "sinvs": case "sivs":
                score = s.getSi(); break;
                
            case "su": case "lichsu": case "m5": case "suvs":
                score = s.getSu(); break;
                
            case "di": case "dia": case "dialy": case "diali": case "m6": case "divs":
                score = s.getDi(); break;
                
            case "v": case "va": case "van": case "nguvan": case "vavs":
                score = s.getVa(); break;
                
            case "a": case "an": case "anh": case "tanh": case "tienganh": case "n1": case "n1thi": case "m8": case "n1vs":
                BigDecimal thi = s.getN1Thi();
                BigDecimal cc = s.getN1Cc();
                if (thi == null) score = cc;
                else if (cc == null) score = thi;
                else score = thi.max(cc);
                break;
                
            case "n1cc": 
                score = s.getN1Cc(); break;
                
            case "ti": case "tin": case "tinhoc": 
                score = s.getTi(); break;
                
            case "ktpl": case "gdcd": case "kinhtephapluat": case "giaoduccongdan": 
                score = s.getKtpl(); break;
                
            case "nl1": case "nluc1": case "danhgianangluc": case "dgnl": 
                score = s.getNl1(); break;
                
            case "cncn": case "cncongnghiep": case "congnghecongnghiep": 
                score = s.getCncn(); break;
                
            case "cnnn": case "cnnongnghiep": case "congnghenongnghiep": 
                score = s.getCnnn(); break;
                
            case "nk1": case "nkhieu1": case "nangkhieu1": 
                score = s.getNk1(); break;
                
            case "nk2": case "nkhieu2": case "nangkhieu2": 
                score = s.getNk2(); break;
                
            case "nk3": case "nkhieu3": case "nangkhieu3": 
                score = s.getNk3(); break;
                
            case "nk4": case "nkhieu4": case "nangkhieu4": 
                score = s.getNk4(); break;
                
            default: 
                break;
        }
        return score;
    }

    private static class BestScoreResult {
        BigDecimal score;
        String combination;
        String method;
        BestScoreResult(BigDecimal s, String c, String m) { 
            this.score = s; 
            this.combination = c; 
            this.method = m;
        }
    }

    private static final Map<String, Map<String, BigDecimal>> DEVIATION_TABLE = new HashMap<>();
    static {
        Map<String, BigDecimal> rowA00 = new HashMap<>();
        rowA00.put("A01", new BigDecimal("-0.69"));
        rowA00.put("B00", new BigDecimal("-1.21"));
        rowA00.put("C00", new BigDecimal("2.32"));
        rowA00.put("C01", new BigDecimal("0.94"));
        rowA00.put("D01", new BigDecimal("-0.68"));
        rowA00.put("D07", new BigDecimal("-1.62"));
        DEVIATION_TABLE.put("A00", rowA00);
        
        Map<String, BigDecimal> rowA01 = new HashMap<>();
        rowA01.put("A00", new BigDecimal("0.69"));
        rowA01.put("B00", new BigDecimal("-0.52"));
        rowA01.put("C00", new BigDecimal("3.01"));
        rowA01.put("C01", new BigDecimal("1.63"));
        rowA01.put("D01", new BigDecimal("0.01"));
        rowA01.put("D07", new BigDecimal("-0.93"));
        DEVIATION_TABLE.put("A01", rowA01);
        
        Map<String, BigDecimal> rowB00 = new HashMap<>();
        rowB00.put("A00", new BigDecimal("1.21"));
        rowB00.put("A01", new BigDecimal("0.52"));
        rowB00.put("C00", new BigDecimal("3.53"));
        rowB00.put("C01", new BigDecimal("2.15"));
        rowB00.put("D01", new BigDecimal("0.53"));
        rowB00.put("D07", new BigDecimal("-0.41"));
        DEVIATION_TABLE.put("B00", rowB00);
        
        Map<String, BigDecimal> rowC00 = new HashMap<>();
        rowC00.put("A00", new BigDecimal("-2.32"));
        rowC00.put("A01", new BigDecimal("-3.01"));
        rowC00.put("B00", new BigDecimal("-3.53"));
        rowC00.put("C01", new BigDecimal("-1.38"));
        rowC00.put("D01", new BigDecimal("-3.00"));
        rowC00.put("D07", new BigDecimal("-3.94"));
        DEVIATION_TABLE.put("C00", rowC00);
        
        Map<String, BigDecimal> rowC01 = new HashMap<>();
        rowC01.put("A00", new BigDecimal("-0.94"));
        rowC01.put("A01", new BigDecimal("-1.63"));
        rowC01.put("B00", new BigDecimal("-2.15"));
        rowC01.put("C00", new BigDecimal("1.38"));
        rowC01.put("D01", new BigDecimal("-1.62"));
        rowC01.put("D07", new BigDecimal("-2.56"));
        DEVIATION_TABLE.put("C01", rowC01);
        
        Map<String, BigDecimal> rowD01 = new HashMap<>();
        rowD01.put("A00", new BigDecimal("0.68"));
        rowD01.put("A01", new BigDecimal("-0.01"));
        rowD01.put("B00", new BigDecimal("-0.53"));
        rowD01.put("C00", new BigDecimal("3.00"));
        rowD01.put("C01", new BigDecimal("1.62"));
        rowD01.put("D07", new BigDecimal("-0.94"));
        DEVIATION_TABLE.put("D01", rowD01);
    }

    private BigDecimal getDeviation(String combination, String rootCombination) {
        if (combination == null || rootCombination == null || combination.equals(rootCombination)) {
            return BigDecimal.ZERO;
        }
        Map<String, BigDecimal> row = DEVIATION_TABLE.get(rootCombination);
        if (row == null) return BigDecimal.ZERO;
        return row.getOrDefault(combination, BigDecimal.ZERO);
    }

    private BigDecimal calculateScaledPriority(BigDecimal basePlusBonus, BigDecimal rawPriority) {
        if (basePlusBonus == null || rawPriority == null || rawPriority.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        if (basePlusBonus.compareTo(BigDecimal.valueOf(22.5)) < 0) {
            return rawPriority.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal remaining = BigDecimal.valueOf(30).subtract(basePlusBonus);
        if (remaining.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return remaining
                .multiply(rawPriority)
                .divide(BigDecimal.valueOf(7.5), 5, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void prepare(Aspiration aspiration) {
        aspiration.setCccd(required(aspiration.getCccd(), "CCCD"));
        if (aspiration.getNganhId() == null) throw new RuntimeException("Ma nganh is required.");
        if (aspiration.getThuTu() == null || aspiration.getThuTu() <= 0) {
            throw new RuntimeException("Thu tu nguyen vong phai lon hon 0.");
        }
        aspiration.setPhuongThuc(clean(aspiration.getPhuongThuc()));
        aspiration.setToHop(clean(aspiration.getToHop()));
        aspiration.setKetQua(clean(aspiration.getKetQua()));

        aspiration.setDiemCc(zeroIfNull(aspiration.getDiemCc()));
        aspiration.setDiemUtxt(zeroIfNull(aspiration.getDiemUtxt()));
        
        if (aspiration.getDiemThxt() != null && aspiration.getDiemXetTuyen() == null) {
            BigDecimal basePlusBonus = aspiration.getDiemThxt().add(aspiration.getDiemCc());
            aspiration.setDiemUtqd(calculateScaledPriority(basePlusBonus, aspiration.getDiemUtxt()));
            aspiration.setDiemXetTuyen(basePlusBonus.add(aspiration.getDiemUtqd()));
            aspiration.setDiemCong(aspiration.getDiemCc());
        }

        aspiration.setNvKeys(aspiration.getCccd() + "_" + (aspiration.getThuTu() != null ? aspiration.getThuTu() : "0") + "_" + aspiration.getNganhId() + "_"
                + keyPart(aspiration.getPhuongThuc()) + "_" + keyPart(aspiration.getToHop()));
        if (aspiration.getKetQua() == null) {
            aspiration.setKetQua("chuaxet");
        }
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String keyPart(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public List<Object[]> getSuccessfulCandidatesReport() {
        return repository.findAllSuccessfulWithCandidate();
    }

    public List<Object[]> getAdmissionCountByMethodReport() {
        return repository.countSuccessfulByMethodAndMajor();
    }

    public static class AdmissionResult {
        private final int total;
        private final int passed;
        private final int failed;
        private final int belowFloor;
        private final int missingScore;
        private final int missingMajorConfig;

        public AdmissionResult(
                int total,
                int passed,
                int failed,
                int belowFloor,
                int missingScore,
                int missingMajorConfig) {
            this.total = total;
            this.passed = passed;
            this.failed = failed;
            this.belowFloor = belowFloor;
            this.missingScore = missingScore;
            this.missingMajorConfig = missingMajorConfig;
        }

        public int getTotal() {
            return total;
        }

        public int getPassed() {
            return passed;
        }

        public int getFailed() {
            return failed;
        }

        public int getBelowFloor() {
            return belowFloor;
        }

        public int getMissingScore() {
            return missingScore;
        }

        public int getMissingMajorConfig() {
            return missingMajorConfig;
        }
    }
}
