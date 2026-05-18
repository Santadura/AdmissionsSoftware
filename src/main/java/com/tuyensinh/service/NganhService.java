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

import com.tuyensinh.entity.XtNganh;
import com.tuyensinh.repository.NganhRepository;

public class NganhService {

    private final NganhRepository repo = new NganhRepository();

    public List<XtNganh> getAll() { return repo.findAll(); }

    public List<XtNganh> search(String kw) {
        if (kw == null || kw.isBlank()) return repo.findAll();
        return repo.search(kw.trim());
    }

    public void add(XtNganh n) {
        if (n.getManganh() == null || n.getManganh().isBlank())
            throw new IllegalArgumentException("Mã ngành không được rỗng!");
        if (repo.existsByManganh(n.getManganh()))
            throw new IllegalArgumentException("Mã ngành '" + n.getManganh() + "' đã tồn tại!");
        repo.save(n);
    }

    public void update(XtNganh n) { repo.update(n); }

    public void delete(Integer id) { repo.delete(id); }

    public static class ImportResult {
        public final int successCount;
        public final List<String> errors;
        public ImportResult(int s, List<String> e) { successCount = s; errors = e; }
    }

    public ImportResult importFromExcel(File file) throws Exception {
        List<XtNganh> list = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int count = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            int rowNum = 0;
            for (Row row : sheet) {
                rowNum++;
                if (rowNum <= 2) continue; // bỏ 2 dòng header
                if (row == null) continue;
                String ma = getStr(row, 1); // cột B: Mã ngành
                if (ma == null || !ma.matches("\\d+.*")) continue; // bỏ dòng tổng/rỗng
                try {
                    XtNganh n = new XtNganh();
                    n.setManganh(ma);
                    n.setTennganh(getStr(row, 2)); // cột C: Tên ngành
                    String ct = getStr(row, 3);    // cột D: Chỉ tiêu
                    if (ct != null) n.setNChitieu(
                        Integer.parseInt(ct.replaceAll("[^0-9]", "")));
                    list.add(n);
                    count++;
                } catch (Exception e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }
        if (!list.isEmpty()) repo.saveAll(list);
        return new ImportResult(count, errors);
    }

    private String getStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String val;
        switch (cell.getCellType()) {
            case STRING: val = cell.getStringCellValue().trim(); break;
            case NUMERIC:
                double d = cell.getNumericCellValue();
                val = (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
                break;
            default: return null;
        }
        return val.isEmpty() ? null : val;
    }
}