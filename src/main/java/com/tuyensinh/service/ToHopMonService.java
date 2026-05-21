package com.tuyensinh.service;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tuyensinh.entity.XtToHopMon;
import com.tuyensinh.repository.ToHopMonRepository;

public class ToHopMonService {

    private final ToHopMonRepository repo = new ToHopMonRepository();

    private static final Map<String, String> MON_NAME_MAP = new LinkedHashMap<>();
    static {
        MON_NAME_MAP.put("TO",   "Toán");
        MON_NAME_MAP.put("VA",   "Ngữ văn");
        MON_NAME_MAP.put("LI",   "Vật lí");
        MON_NAME_MAP.put("HO",   "Hóa học");
        MON_NAME_MAP.put("SI",   "Sinh học");
        MON_NAME_MAP.put("SU",   "Lịch sử");
        MON_NAME_MAP.put("DI",   "Địa lí");
        MON_NAME_MAP.put("GD",   "GDCD");
        MON_NAME_MAP.put("KTPL", "Kinh tế pháp luật");
        MON_NAME_MAP.put("TI",   "Tin học");
        MON_NAME_MAP.put("N1",   "Tiếng Anh");
        MON_NAME_MAP.put("NK1",  "Kể chuyện - Đọc diễn cảm");
        MON_NAME_MAP.put("NK2",  "Hát - Nhạc");
        MON_NAME_MAP.put("NK3",  "Hình họa");
        MON_NAME_MAP.put("NK4",  "Trang trí");
        MON_NAME_MAP.put("NK5",  "Hát - Nhạc cụ");
        MON_NAME_MAP.put("NK6",  "Xướng âm - Thẩm âm - Tiết tấu");
    }

    private String getMonName(String maMon) {
        if (maMon == null) return null;
        return MON_NAME_MAP.getOrDefault(maMon.toUpperCase(), maMon);
    }


    public List<XtToHopMon> getAll()   { return repo.findAll(); }

    public List<XtToHopMon> search(String kw) {
        if (kw == null || kw.isBlank()) return repo.findAll();
        return repo.search(kw.trim());
    }

    public void add(XtToHopMon t) {
        if (t.getMatohop() == null || t.getMatohop().isBlank())
            throw new IllegalArgumentException("Mã tổ hợp không được rỗng!");
        if (repo.existsByMatohop(t.getMatohop()))
            throw new IllegalArgumentException("Mã tổ hợp '" + t.getMatohop() + "' đã tồn tại!");
        repo.save(t);
    }

    public void update(XtToHopMon t) { repo.update(t); }

    public void delete(Integer id)    { repo.delete(id); }

    public static class ImportResult {
        public final int successCount;
        public final List<String> errors;
        public ImportResult(int s, List<String> e) { successCount = s; errors = e; }
    }


    public ImportResult importFromExcel(File file) throws Exception {
        List<String> errors = new ArrayList<>();
        int count = 0;
        Map<String, Boolean> processed = new LinkedHashMap<>();

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = wb.getSheetAt(0);

            int toHopIdx = -1, headerRow = -1;

            for (int i = 0; i < 5; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String v = norm(getStr(row, j));
                    if (v.contains("ma_to_hop") || v.contains("to hop") || v.contains("tohop"))
                        toHopIdx = j;
                }
                if (toHopIdx != -1) { headerRow = i; break; }
            }

            if (toHopIdx == -1)
                return new ImportResult(0, List.of("Không tìm thấy cột 'MA_TO_HOP' trong file!"));

            for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getPhysicalNumberOfCells() == 0) continue;

                String maFull = getStr(row, toHopIdx);
                if (maFull == null) continue;

                try {
                    int parenIdx = maFull.indexOf('(');
                    String maTohop = (parenIdx > 0)
                            ? maFull.substring(0, parenIdx).trim()
                            : maFull.trim();

                    if (maTohop.isEmpty()) continue;
                    if (processed.containsKey(maTohop)) continue; 
                    processed.put(maTohop, true);

                    List<String> monCodes = new ArrayList<>();
                    if (parenIdx > 0) {
                        int endIdx = maFull.lastIndexOf(')');
                        if (endIdx > parenIdx) {
                            String inside = maFull.substring(parenIdx + 1, endIdx);
                            for (String part : inside.split(",")) {
                                String ma = part.split("-")[0].trim().toUpperCase();
                                if (!ma.isEmpty()) monCodes.add(ma);
                            }
                        }
                    }

                    String ma1 = monCodes.size() > 0 ? monCodes.get(0) : null;
                    String ma2 = monCodes.size() > 1 ? monCodes.get(1) : null;
                    String ma3 = monCodes.size() > 2 ? monCodes.get(2) : null;

                    StringBuilder tenBuilder = new StringBuilder();
                    if (ma1 != null) tenBuilder.append(getMonName(ma1));
                    if (ma2 != null) tenBuilder.append(", ").append(getMonName(ma2));
                    if (ma3 != null) tenBuilder.append(", ").append(getMonName(ma3));

                    XtToHopMon t = new XtToHopMon();
                    t.setMatohop(maTohop);
                    t.setMon1(ma1);
                    t.setMon2(ma2);
                    t.setMon3(ma3);
                    t.setTentohop(tenBuilder.toString());

                    repo.mergeByMatohop(t);
                    count++;

                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        return new ImportResult(count, errors);
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
        String val = switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            }
            default -> null;
        };
        return (val == null || val.isEmpty()) ? null : val;
    }
}