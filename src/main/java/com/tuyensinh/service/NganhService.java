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

    public XtNganh getById(Integer id) { return repo.findById(id); }

    public List<XtNganh> getAll() { return repo.findAll(); }

    public List<Object[]> getAllWithStats() {
        return repo.findAllWithAspirationCount();
    }

    public List<Object[]> searchWithStats(String kw) {
        if (kw == null || kw.isBlank()) return getAllWithStats();
        return repo.searchWithAspirationCount(kw.trim());
    }

    public List<XtNganh> search(String kw) {
        if (kw == null || kw.isBlank()) return repo.findAll();
        return repo.search(kw.trim());
    }

    public XtNganh searchByMaNganh(String manganh) {
        return repo.findByManganh(manganh);
    }

    public void add(XtNganh n) {
        if (n.getManganh() == null || n.getManganh().isBlank())
            throw new IllegalArgumentException("Mã ngành không được rỗng!");
        if (n.getNamTuyenSinh() == null) {
            n.setNamTuyenSinh(2025);
        }
        if (repo.existsByManganh(n.getManganh(), n.getNamTuyenSinh()))
            throw new IllegalArgumentException("Mã ngành '" + n.getManganh() + "' năm " + n.getNamTuyenSinh() + " đã tồn tại!");
        repo.save(n);
    }

    public void update(XtNganh n) {
        if (n.getNamTuyenSinh() == null) {
            n.setNamTuyenSinh(2025);
        }
        repo.update(n);
    }

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
                    n.setNamTuyenSinh(2025);
                    
                    String ds = getStr(row, 3);    // cột D: Ngưỡng đầu vào / Điểm sàn
                    if (ds != null) {
                        try {
                            n.setNDiemsan(new java.math.BigDecimal(ds.replace(",", ".").replaceAll("[^0-9.]", "")));
                        } catch (Exception e) {
                            // Bỏ qua nếu không parse được điểm
                        }
                    }

                    // Xử lý định dạng file 8 cột: tìm chữ "Gốc" ở cột G (index 6) để lấy tổ hợp gốc ở cột F (index 5)
                    String checkGoc = getStr(row, 6);
                    if (checkGoc != null && checkGoc.toLowerCase().contains("gốc")) {
                        String toHopGoc = getStr(row, 5);
                        if (toHopGoc != null) {
                            n.setNTohopgoc(toHopGoc.trim().toUpperCase());
                        }
                    }

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