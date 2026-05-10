package com.tuyensinh.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tuyensinh.entity.XtToHopMon;
import com.tuyensinh.repository.ToHopMonRepository;

public class ToHopMonService {

    private final ToHopMonRepository repo = new ToHopMonRepository();

    public List<XtToHopMon> getAll() { return repo.findAll(); }

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

    public void delete(Integer id) { repo.delete(id); }

    public static class ImportResult {
        public final int successCount;
        public final List<String> errors;
        public ImportResult(int s, List<String> e) { successCount = s; errors = e; }
    }

    public ImportResult importFromExcel(File file) throws Exception {
        // Dùng Map để gom unique theo matohop (tránh trùng lặp)
        Map<String, XtToHopMon> map = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        // Pattern parse: "B03(TO-3,VA-3,SI-1)" -> matohop=B03, mon1=TO, mon2=VA, mon3=SI
        // Cột F (index 5) là TEN_TO_HOP = matohop thực sự (VD: "B03")
        // Cột D (index 3) là MA_TO_HOP đầy đủ để parse ra mon1,mon2,mon3
        Pattern monPattern = Pattern.compile("([A-Z0-9]+)-[\\d.]+");

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            int rowNum = 0;
            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // bỏ header
                if (row == null) continue;

                String maTohop = getStr(row, 5); // cột F: TEN_TO_HOP = mã tổ hợp (B03, A00,...)
                String maFull  = getStr(row, 3); // cột D: MA_TO_HOP đầy đủ (B03(TO-3,VA-3,SI-1))

                if (maTohop == null || maFull == null) continue;
                if (map.containsKey(maTohop)) continue; // đã có rồi, bỏ qua

                try {
                    // Parse các môn từ chuỗi trong ngoặc: "B03(TO-3,VA-3,SI-1)"
                    List<String> monList = new ArrayList<>();
                    int start = maFull.indexOf('(');
                    int end   = maFull.lastIndexOf(')');
                    if (start != -1 && end != -1) {
                        String inside = maFull.substring(start + 1, end);
                        Matcher m = monPattern.matcher(inside);
                        while (m.find()) monList.add(m.group(1));
                    }

                    XtToHopMon t = new XtToHopMon();
                    t.setMatohop(maTohop);
                    t.setMon1(monList.size() > 0 ? monList.get(0) : null);
                    t.setMon2(monList.size() > 1 ? monList.get(1) : null);
                    t.setMon3(monList.size() > 2 ? monList.get(2) : null);
                    t.setTentohop(maTohop); // dùng mã làm tên (VD: "B03")
                    map.put(maTohop, t);
                } catch (Exception e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }

        List<XtToHopMon> list = new ArrayList<>(map.values());
        if (!list.isEmpty()) repo.saveAll(list);
        return new ImportResult(list.size(), errors);
    }

    private String getStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String val;
        switch (cell.getCellType()) {
            case STRING:  val = cell.getStringCellValue().trim(); break;
            case NUMERIC:
                double d = cell.getNumericCellValue();
                val = (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
                break;
            default: return null;
        }
        return val.isEmpty() ? null : val;
    }
}