package com.tuyensinh.service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.*;
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

    public ImportResult importDiemSan(File file) throws Exception {
        List<String> errors = new ArrayList<>();
        List<XtNganh> listToImport = new ArrayList<>();
        int count = 0;
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = wb.getSheetAt(0);
            int maIdx = -1, tenIdx = -1, diemIdx = -1, headerRow = -1;
            for (int i = 0; i < 5; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String v = norm(getStr(row, j));
                    if      (v.contains("xet") || v.contains("ma nganh"))             maIdx   = j;
                    else if (v.contains("ten nganh") || v.contains("chuong trinh"))   tenIdx  = j;
                    else if (v.contains("vao") || v.contains("diem san"))             diemIdx = j;
                }
                if (maIdx != -1) { headerRow = i; break; }
            }
            if (maIdx  == -1) return new ImportResult(0, List.of("File 1: Không tìm thấy cột 'Mã xét tuyển'!"));
            if (diemIdx == -1) return new ImportResult(0, List.of("File 1: Không tìm thấy cột 'Ngưỡng đầu vào'!"));

            for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String ma = getStr(row, maIdx);
                if (!isValidMa(ma)) continue;
                try {
                    XtNganh n = createNewNganh(ma);
                    
                    setIfNotBlank(getStr(row, tenIdx), n::setTennganh);
                    String ds = getStr(row, diemIdx);
                    if (ds != null) n.setNDiemsan(new BigDecimal(ds.replace(",", ".")));
                    listToImport.add(n);
                    count++;
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        if (!listToImport.isEmpty()) {
            repo.importBatch(listToImport);
        }
        return new ImportResult(count, errors);
    }

    public ImportResult importToHop(File file) throws Exception {
        List<String> errors = new ArrayList<>();
        List<XtNganh> listToImport = new ArrayList<>();
        int count = 0;

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = wb.getSheetAt(0);
            int maIdx = -1, tenIdx = -1, toHopIdx = -1, gocIdx = -1, headerRow = -1;

            for (int i = 0; i < 5; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String v = norm(getStr(row, j));
                    if      (v.equals("manganh"))                                      maIdx    = j;
                    else if (v.contains("ten_nganh") || v.contains("ten nganh"))       tenIdx   = j;
                    else if (v.equals("ma_to_hop")   || v.contains("to hop"))          toHopIdx = j;
                    else if (v.equals("goc"))                                          gocIdx   = j;
                }
                if (maIdx != -1) { headerRow = i; break; }
            }

            if (maIdx == -1)
                return new ImportResult(0, List.of("File 2: Không tìm thấy cột 'MANGANH'!"));
            if (toHopIdx == -1)
                return new ImportResult(0, List.of("File 2: Không tìm thấy cột 'MA_TO_HOP'!"));

            for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String ma = getStr(row, maIdx);
                if (!isValidMa(ma)) continue;

                if (gocIdx != -1 && !norm(getStr(row, gocIdx)).contains("goc")) continue;

                try {
                    XtNganh n = createNewNganh(ma);
                    
                    setIfNotBlank(getStr(row, tenIdx), n::setTennganh);
                    String th = getStr(row, toHopIdx);
                    if (th != null && th.length() >= 3)
                        n.setNTohopgoc(th.substring(0, 3).trim());
                    listToImport.add(n);
                    count++;
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        if (!listToImport.isEmpty()) {
            repo.importBatch(listToImport);
        }
        return new ImportResult(count, errors);
    }

    public ImportResult importChiTieu(File file) throws Exception {
        List<String> errors = new ArrayList<>();
        List<XtNganh> listToImport = new ArrayList<>();
        int count = 0;

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = wb.getSheetAt(0);
            int maIdx = -1, tenIdx = -1, chiTieuIdx = -1, headerRow = -1;

            for (int i = 0; i < 5; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String v = norm(getStr(row, j));
                    if (v.contains("ma ctdt") || v.contains("ma ct") || v.equals("ma nganh")) maIdx = j;
                    else if (v.contains("ten ctdt") || v.contains("ten ct") || v.contains("ten nganh")) tenIdx = j;
                    else if (v.contains("chi tieu"))                             chiTieuIdx = j;
                }
                if (maIdx != -1) { headerRow = i; break; }
            }

            if (maIdx == -1)
                return new ImportResult(0, List.of("File 3: Không tìm thấy cột 'Mã CTĐT'!"));
            if (chiTieuIdx == -1)
                return new ImportResult(0, List.of("File 3: Không tìm thấy cột 'Chỉ tiêu'!"));

            for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String ma = getStr(row, maIdx); 
                if (!isValidMa(ma)) continue;

                try {
                    XtNganh n = createNewNganh(ma);
                    
                    setIfNotBlank(getStr(row, tenIdx), n::setTennganh);
                    String ct = getStr(row, chiTieuIdx);
                    if (ct != null && !ct.isBlank())
                        n.setNChitieu(Integer.parseInt(ct.replaceAll("[^0-9]", "")));
                    listToImport.add(n);
                    count++;
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        if (!listToImport.isEmpty()) {
            repo.importBatch(listToImport);
        }
        return new ImportResult(count, errors);
    }

    public ImportResult importFromExcel(File file) throws Exception {
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 0; i < 5; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                boolean hasMa = false, hasToHop = false, hasChiTieu = false, hasDiem = false;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String v = norm(getStr(row, j));
                    if (v.equals("manganh") || v.contains("ma ct") || v.contains("xet")) hasMa = true;
                    if (v.contains("to hop") || v.contains("to_hop"))                    hasToHop = true;
                    if (v.contains("tieu"))                                              hasChiTieu = true;
                    if (v.contains("vao") || v.contains("dau vao") || v.contains("diem san")) hasDiem = true;
                }

                if (!hasMa) continue; 
                if (hasToHop)   return importToHop(file);
                if (hasChiTieu) return importChiTieu(file);
                if (hasDiem)    return importDiemSan(file);
            }
        }
        return new ImportResult(0, List.of("Không nhận diện được loại file!"));
    }

    private XtNganh createNewNganh(String maNganh) {
        XtNganh n = new XtNganh();
        n.setManganh(maNganh);
        
        n.setNChitieu(-1); 
        
        n.setNThpt("UNMODIFIED");
        n.setNDgnl("UNMODIFIED");
        n.setNVsat("UNMODIFIED");
        
        return n;
    }

    private void setIfNotBlank(String val, Consumer<String> setter) {
        if (val != null && !val.isBlank()) setter.accept(val);
    }

    private boolean isValidMa(String ma) {
        return ma != null && ma.matches("\\d+.*");
    }

    private String norm(String s) {
        if (s == null) return "";
        s = s.replace('đ', 'd').replace('Đ', 'D'); 
        s = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replaceAll("[^a-z0-9 _]", " ").replaceAll("\\s+", " ").trim(); 
        return s;
    }

    private String getStr(Row row, int col) {
        if (col < 0 || row == null) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        
        String val = null;
        
        switch (cell.getCellType()) {
            case STRING:
                val = cell.getStringCellValue().trim();
                break;
            case NUMERIC:
                double d = cell.getNumericCellValue();
                val = (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
                break;
            default:
                val = null;
                break;
        }
        
        return (val == null || val.isEmpty()) ? null : val;
    }
}
