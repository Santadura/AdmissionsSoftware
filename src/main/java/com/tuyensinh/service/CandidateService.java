package com.tuyensinh.service;

import com.tuyensinh.entity.Candidate;
import com.tuyensinh.repository.CandidateRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class CandidateService {
    
    private final CandidateRepository repository;
    private static final int PAGE_SIZE = 20;
    
    public CandidateService() {
        this.repository = new CandidateRepository();
    }
    
    public ImportResult importFromExcel(File file) throws Exception {
        List<Candidate> candidates = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            int rowNum = 0;
            for (Row row : sheet) {
                rowNum++;
                
                if (rowNum == 1) continue;
                if (row == null || row.getCell(1) == null) continue;
                
                try {
                    Candidate candidate = new Candidate();
                    
                    candidate.setCccd(getCellStringValue(row, 1));
                    
                    String hoTen = getCellStringValue(row, 2);
                    if (hoTen != null && !hoTen.trim().isEmpty()) {
                        String[] parts = hoTen.trim().split("\\s+", 2);
                        if (parts.length == 2) {
                            candidate.setHo(parts[0]);
                            candidate.setTen(parts[1]);
                        } else {
                            candidate.setHo(hoTen.trim());
                            candidate.setTen("");
                        }
                    }
                    
                    candidate.setNgaySinh(getCellStringValue(row, 3));
                    candidate.setGioiTinh(getCellStringValue(row, 4));
                    candidate.setDoiTuong(getCellStringValue(row, 5));
                    candidate.setKhuVuc(getCellStringValue(row, 6));
                    candidate.setNoiSinh(getCellStringValue(row, 35));
                    candidate.setSobaodanh(candidate.getCccd());
                    
                    if (candidate.getCccd() == null || candidate.getCccd().trim().isEmpty()) {
                        errors.add("Dòng " + rowNum + ": CCCD trống, bỏ qua");
                        continue;
                    }
                    
                    candidates.add(candidate);
                    successCount++;
                    
                    if (candidates.size() >= 500) {
                        repository.saveAll(candidates);
                        candidates.clear();
                    }
                    
                } catch (Exception e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
            
            if (!candidates.isEmpty()) {
                repository.saveAll(candidates);
            }
        }
        
        return new ImportResult(successCount, errors);
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
    
    private String getCellStringValue(Row row, int colIndex) {
        if (row == null || colIndex < 0) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                String val = cell.getStringCellValue().trim();
                return val.isEmpty() ? null : val;
            case NUMERIC:
                double numVal = cell.getNumericCellValue();
                if (numVal == Math.floor(numVal) && !Double.isInfinite(numVal)) {
                    return String.valueOf((long) numVal);
                }
                return String.valueOf(numVal);
            default:
                return null;
        }
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