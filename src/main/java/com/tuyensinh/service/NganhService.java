package com.tuyensinh.service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tuyensinh.entity.XtNganh;
import com.tuyensinh.repository.NganhRepository;

public class NganhService {

    private final NganhRepository repo = new NganhRepository();

    public List<XtNganh> getAll() {
        return repo.findAll();
    }

    public List<XtNganh> search(String kw) {
        if (kw == null || kw.isBlank()) return repo.findAll();
        return repo.search(kw.trim());
    }

    public void add(XtNganh n) {
        if (n.getManganh() == null || n.getManganh().isBlank()) {
            throw new IllegalArgumentException("Mã ngành không được rỗng!");
        }
        if (repo.existsByManganh(n.getManganh())) {
            throw new IllegalArgumentException("Mã ngành '" + n.getManganh() + "' đã tồn tại!");
        }
        repo.save(n);
    }

    public void update(XtNganh n) {
        repo.update(n);
    }

    public void delete(Integer id) {
        repo.delete(id);
    }

    public static class ImportResult {
        public final int successCount;
        public final List<String> errors;

        public ImportResult(int successCount, List<String> errors) {
            this.successCount = successCount;
            this.errors = errors;
        }
    }

    private enum FileType {
        CHI_TIEU,
        NGUONG_DAU_VAO,
        TO_HOP_GOC
    }

    public ImportResult importFromExcel(File file) throws Exception {
        FileType type = detectFileType(file);

        switch (type) {
            case CHI_TIEU:
                return importChiTieu(file);
            case NGUONG_DAU_VAO:
                return importNguongDauVao(file);
            case TO_HOP_GOC:
                return importToHopGoc(file);
            default:
                throw new IllegalArgumentException("Không nhận diện được loại file Excel.");
        }
    }

    private FileType detectFileType(File file) throws Exception {
        String fileName = normalize(file.getName());

        if (fileName.contains("chi-tieu") || fileName.contains("chi tieu")) {
            return FileType.CHI_TIEU;
        }
        if (fileName.contains("nguong")) {
            return FileType.NGUONG_DAU_VAO;
        }
        if (fileName.contains("tohop") || fileName.contains("to hop")) {
            return FileType.TO_HOP_GOC;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            for (int i = 0; i <= Math.min(sheet.getLastRowNum(), 5); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String rowText = "";
                for (int j = 0; j < 10; j++) {
                    String cell = getStr(row, j);
                    if (cell != null) {
                        rowText += " " + normalize(cell);
                    }
                }

                if (rowText.contains("chi tieu") || rowText.contains("chi tieu chot") || rowText.contains("ma ctdt")) {
                    return FileType.CHI_TIEU;
                }

                if (rowText.contains("nguong dau vao") || rowText.contains("ma xet tuyen")) {
                    return FileType.NGUONG_DAU_VAO;
                }

                if (rowText.contains("ma to hop") || rowText.contains("to hop") || rowText.contains("goc")) {
                    return FileType.TO_HOP_GOC;
                }
            }
        }

        throw new IllegalArgumentException(
            "Không nhận diện được loại file Excel từ tên file hoặc header. " +
            "File hỗ trợ: chỉ tiêu, ngưỡng đầu vào, tổ hợp gốc."
        );
    }

    private ImportResult importChiTieu(File file) throws Exception {
        List<XtNganh> list = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int count = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (rowNum <= 2) continue;
                if (row == null) continue;

                String ma = getStr(row, 1);
                if (ma == null || !ma.matches("\\d+.*")) continue;

                try {
                    XtNganh n = new XtNganh();
                    n.setManganh(ma.trim());
                    n.setTennganh(getStr(row, 2));

                    String ct = getStr(row, 3);
                    if (ct != null && !ct.isBlank()) {
                        String so = ct.replaceAll("[^0-9]", "");
                        n.setNChitieu(so.isEmpty() ? null : Integer.parseInt(so));
                    }

                    list.add(n);
                    count++;
                } catch (Exception e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }

        if (!list.isEmpty()) {
            repo.saveAllChiTieu(list);
        }

        return new ImportResult(count, errors);
    }

    private ImportResult importNguongDauVao(File file) throws Exception {
        List<XtNganh> list = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int count = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue;
                if (row == null) continue;

                String ma = getStr(row, 1);
                if (ma == null || !ma.matches("\\d+.*")) continue;

                try {
                    XtNganh n = new XtNganh();
                    n.setManganh(ma.trim());
                    n.setTennganh(getStr(row, 2));

                    String diem = getStr(row, 3);
                    if (diem != null && !diem.isBlank()) {
                        n.setNDiemsan(new BigDecimal(diem.trim()));
                    }

                    list.add(n);
                    count++;
                } catch (Exception e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }

        if (!list.isEmpty()) {
            repo.saveAllNguongDauVao(list);
        }

        return new ImportResult(count, errors);
    }

    private ImportResult importToHopGoc(File file) throws Exception {
        List<XtNganh> list = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int count = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue;
                if (row == null) continue;

                String maNganh = getStr(row, 1);
                String maToHop = getStr(row, 5);
                String loai = getStr(row, 6);

                if (maNganh == null || maNganh.isBlank()) continue;
                if (maToHop == null || maToHop.isBlank()) continue;

                try {
                    if (loai != null && normalize(loai).equals("goc")) {
                        XtNganh n = new XtNganh();
                        n.setManganh(maNganh.trim());
                        n.setTennganh(getStr(row, 2));
                        n.setNTohopgoc(maToHop.trim());

                        list.add(n);
                        count++;
                    }
                } catch (Exception e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }

        if (!list.isEmpty()) {
            repo.saveAllToHopGoc(list);
        }

        return new ImportResult(count, errors);
    }

    private String getStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;

        String val;
        switch (cell.getCellType()) {
            case STRING:
                val = cell.getStringCellValue().trim();
                break;
            case NUMERIC:
                double d = cell.getNumericCellValue();
                val = (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
                break;
            case BOOLEAN:
                val = String.valueOf(cell.getBooleanCellValue());
                break;
            default:
                return null;
        }

        return val.isEmpty() ? null : val;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim();
        return t.replaceAll("\\s+", " ");
    }
}