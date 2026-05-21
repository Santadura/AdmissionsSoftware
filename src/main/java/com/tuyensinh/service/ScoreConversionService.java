package com.tuyensinh.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tuyensinh.entity.ScoreConversion;
import com.tuyensinh.repository.ScoreConversionRepository;

public class ScoreConversionService {
    private ScoreConversionRepository repository;

    public ScoreConversionService() {
        this.repository = new ScoreConversionRepository();
    }

    public List<ScoreConversion> getAll() {
        return repository.findAll();
    }

    public List<ScoreConversion> search(String term) {
        return repository.search(term);
    }

    public void save(ScoreConversion sc) {
        repository.saveOrUpdate(sc);
    }

    public void delete(int id) {
        repository.delete(id);
    }

    public void importExcel(File file) throws Exception {
        List<ScoreConversion> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                
                ScoreConversion sc = new ScoreConversion();
                sc.setPhuongThuc(getCellValue(row, 0));
                sc.setToHop(getCellValue(row, 1));
                sc.setMon(getCellValue(row, 2));
                sc.setDiemA(getDoubleValue(row, 3));
                sc.setDiemB(getDoubleValue(row, 4));
                sc.setDiemC(getDoubleValue(row, 5));
                sc.setDiemD(getDoubleValue(row, 6));
                sc.setMaQuyDoi(getCellValue(row, 7));
                sc.setPhanVi(getCellValue(row, 8));
                
                if (sc.getPhuongThuc() != null) {
                    list.add(sc);
                }
            }
        }
        repository.saveAll(list);
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return null;
        }
    }

    private Double getDoubleValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        try {
            return Double.parseDouble(cell.getStringCellValue());
        } catch (Exception e) {
            return 0.0;
        }
    }
}