package com.tuyensinh.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.repository.CandidateRepository;

public class CandidateService {
    
    private final CandidateRepository repository;
    private static final int PAGE_SIZE = 20;
    private final java.util.Map<String, Candidate> cccdCache = java.util.Collections.synchronizedMap(new java.util.LinkedHashMap<>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<String, Candidate> eldest) {
            return size() > 500; // Lưu tối đa 500 thí sinh gần nhất
        }
    });

    private final java.util.Map<String, Long> negativeCache = java.util.Collections.synchronizedMap(new java.util.LinkedHashMap<>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<String, Long> eldest) {
            return size() > 500; // Lưu tối đa 500 CCCD không tồn tại
        }
    });

    public CandidateService() {
        this.repository = new CandidateRepository();
    }

    public Candidate getByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return null;
        String trimmedCccd = cccd.trim();

        if (cccdCache.containsKey(trimmedCccd)) return cccdCache.get(trimmedCccd);

        // Kiểm tra negative cache (hết hạn sau 5 giây)
        Long lastCheck = negativeCache.get(trimmedCccd);
        if (lastCheck != null && (System.currentTimeMillis() - lastCheck < 5000)) {
            return null;
        }

        Candidate c = repository.findByCccd(trimmedCccd);
        
        // Nếu không tìm thấy, thử tìm phiên bản mất số 0 ở đầu (do lỗi import trước đó)
        if (c == null && trimmedCccd.startsWith("0") && trimmedCccd.matches("\\d+")) {
            String unpadded = trimmedCccd.replaceFirst("^0+", "");
            if (!unpadded.isEmpty()) {
                c = repository.findByCccd(unpadded);
            }
        }

        if (c != null) {
            cccdCache.put(trimmedCccd, c);
        } else {
            negativeCache.put(trimmedCccd, System.currentTimeMillis());
        }
        return c;
    }    
    public ImportResult importFromExcel(File file) throws Exception {
        List<Candidate> candidates = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new Exception("File Excel không có dữ liệu hoặc trống dòng đầu tiên.");

            java.util.Map<String, Integer> headerMap = new java.util.HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null) {
                    String header = "";
                    if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                        header = cell.getStringCellValue().trim().toUpperCase();
                    } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                        header = String.valueOf((long) cell.getNumericCellValue()).toUpperCase();
                    }
                    if (!header.isEmpty()) {
                        headerMap.put(header, cell.getColumnIndex());
                    }
                }
            }

            int cccdCol = findColumn(headerMap, "CCCD", "CMND", "SỐ CCCD", "MÃ ĐỊNH DANH", "SOCCCD");
            if (cccdCol < 0) {
                throw new Exception("Không tìm thấy cột CCCD trong file Excel.");
            }
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                
                try {
                    String cccd = getCellStringValue(row, cccdCol);
                    if (cccd == null || cccd.trim().isEmpty()) continue;

                    Candidate candidate = new Candidate();
                    candidate.setCccd(cccd);
                    
                    String sbd = getCellStringValue(row, findColumn(headerMap, "SBD", "SỐ BÁO DANH", "SOBAODANH", "MÃ SỐ THI"));
                    candidate.setSobaodanh(sbd != null ? sbd : cccd);
                    
                    String hoTen = getCellStringValue(row, findColumn(headerMap, "HO_TEN", "HỌ VÀ TÊN", "HOTEN", "NAME", "FULLNAME", "HỌ TÊN"));
                    if (hoTen != null && !hoTen.trim().isEmpty()) {
                        String[] parts = hoTen.trim().split("\\s+", 2);
                        if (parts.length == 2) {
                            candidate.setHo(parts[0]);
                            candidate.setTen(parts[1]);
                        } else {
                            candidate.setHo(hoTen.trim());
                            candidate.setTen("");
                        }
                    } else {
                        String ho = getCellStringValue(row, findColumn(headerMap, "HO", "HỌ", "LASTNAME"));
                        String ten = getCellStringValue(row, findColumn(headerMap, "TEN", "TÊN", "FIRSTNAME"));
                        
                        if ((ho == null || ho.isEmpty()) && (ten == null || ten.isEmpty())) {
                            candidate.setHo(candidate.getSobaodanh());
                            candidate.setTen("");
                        } else {
                            candidate.setHo(ho != null ? ho : "");
                            candidate.setTen(ten != null ? ten : "");
                        }
                    }
                    
                    candidate.setNgaySinh(parseDate(getCellStringValue(row, findColumn(headerMap, "NGAY_SINH", "NGAYSINH", "NGÀY SINH", "NGÀY_SINH", "DOB", "BIRTHDAY"))));
                    candidate.setGioiTinh(getCellStringValue(row, findColumn(headerMap, "GIOI_TINH", "GIOITINH", "GIỚI TÍNH", "GIOI TÍNH", "GENDER", "GT", "PHÁI", "NAM/NỮ")));
                    candidate.setDoiTuong(getCellStringValue(row, findColumn(headerMap, "DOI_TUONG", "DOITUONG", "ĐỐI TƯỢNG", "OBJECT", "ĐTƯV", "ĐTƯT", "ĐT", "ĐỐI TƯỢNG ƯU TIÊN")));
                    candidate.setKhuVuc(getCellStringValue(row, findColumn(headerMap, "KHU_VUC", "KHUVUC", "KHU VỰC", "AREA", "REGION", "KVƯT", "KV", "KHU VỰC ƯU TIÊN")));
                    candidate.setNoiSinh(getCellStringValue(row, findColumn(headerMap, "NOI_SINH", "NOISINH", "NƠI SINH", "POB")));
                    candidate.setDienThoai(getCellStringValue(row, findColumn(headerMap, "DIEN_THOAI", "DIENTHOAI", "ĐIỆN THOẠI", "PHONE", "TEL")));
                    candidate.setEmail(getCellStringValue(row, findColumn(headerMap, "EMAIL", "THU_DIEN_TU")));
                    
                    candidate.setNamTuyenSinh(2025);
                    candidate.setUpdatedAt(java.time.LocalDate.now());
                    
                    candidates.add(candidate);
                    successCount++;
                    
                    if (candidates.size() >= 1000) {
                        repository.saveAll(candidates);
                        candidates.clear();
                    }
                    
                } catch (Exception e) {
                    errors.add("Dòng " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
            
            if (!candidates.isEmpty()) {
                repository.saveAll(candidates);
            }
        }
        
        return new ImportResult(successCount, errors);
    }

    private int findColumn(java.util.Map<String, Integer> headerMap, String... aliases) {
        for (String alias : aliases) {
            Integer col = headerMap.get(alias.toUpperCase());
            if (col != null) return col;
        }
        return -1;
    }

    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return java.time.LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return java.time.LocalDate.parse(dateStr, f);
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    public List<Candidate> getCandidates(int page) {
        return repository.findAll(page, PAGE_SIZE);
    }
    
    public List<Candidate> searchCandidates(String searchTerm, int page) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getCandidates(page);
        }
        return repository.search(searchTerm.trim(), page, PAGE_SIZE);
    }
    
    public long getTotalPages() {
        return (long) Math.ceil((double) repository.count() / PAGE_SIZE);
    }
    
    public long getTotalSearchPages(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getTotalPages();
        }
        return (long) Math.ceil((double) repository.countBySearch(searchTerm.trim()) / PAGE_SIZE);
    }
    
    public Candidate getCandidateById(Integer id) {
        return repository.findById(id);
    }
    
    public void updateCandidate(Candidate candidate) {
        repository.update(candidate);
    }

    public void deleteCandidate(Candidate candidate) {
        repository.delete(candidate);
    }

    public void saveCandidate(Candidate candidate) {
        repository.save(candidate);
    }

    public long getTotalCandidates() {
        return repository.count();
    }

    public List<Object[]> getStatisticsByObject() {
        return repository.getStatisticsByObject();
    }

    public List<Object[]> getStatisticsByRegion() {
        return repository.getStatisticsByRegion();
    }
    
    private String getCellStringValue(Row row, int colIndex) {
        if (row == null || colIndex < 0) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        
        String val = "";
        switch (cell.getCellType()) {
            case STRING:
                val = cell.getStringCellValue().trim();
                break;
            case NUMERIC:
                double numVal = cell.getNumericCellValue();
                if (numVal == Math.floor(numVal) && !Double.isInfinite(numVal)) {
                    val = String.valueOf((long) numVal);
                } else {
                    val = String.valueOf(numVal);
                }
                break;
            default:
                return null;
        }

        if (val.isEmpty()) return null;

        // Xử lý mất số 0 ở đầu cho CCCD (thường có 12 số hoặc 9 số cũ)
        if (colIndex == 1 && val.matches("\\d+")) {
            if (val.length() > 9 && val.length() < 12) {
                return String.format("%012d", Long.parseLong(val));
            } else if (val.length() < 9) {
                return String.format("%09d", Long.parseLong(val));
            }
        }
        
        return val;
    }
    
    public static class ImportResult {
        public final int successCount;
        public final List<String> errors;
        
        public ImportResult(int successCount, List<String> errors) {
            this.successCount = successCount;
            this.errors = errors;
        }
    }
}