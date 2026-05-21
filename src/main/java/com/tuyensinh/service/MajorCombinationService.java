package com.tuyensinh.service;

import com.tuyensinh.entity.MajorCombination;
import com.tuyensinh.repository.MajorCombinationRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MajorCombinationService {
    private MajorCombinationRepository repository;

    public MajorCombinationService() {
        this.repository = new MajorCombinationRepository();
    }

    public List<MajorCombination> getAll() {
        return repository.findAll();
    }

    public List<MajorCombination> search(String term) {
        return repository.search(term);
    }

    public void save(MajorCombination mc) {
        if (mc.getMaNganh() != null && mc.getMaToHop() != null) {
            mc.setTbKeys(mc.getMaNganh() + "_" + mc.getMaToHop());
        }
        repository.saveOrUpdate(mc);
    }

    public void delete(int id) {
        repository.delete(id);
    }

    public static class ImportResult {
        public final int successCount;
        public final List<String> errors;
        public ImportResult(int s, List<String> e) { successCount = s; errors = e; }
    }

    public ImportResult importFromExcel(File file) throws Exception {
        List<MajorCombination> listToImport = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int count = 0;

        Map<String, MajorCombination> existingMap = new HashMap<>();
        for (MajorCombination mc : repository.findAll()) {
            if (mc.getTbKeys() != null) {
                existingMap.put(mc.getTbKeys(), mc);
            }
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {
             
            Sheet sheet = wb.getSheetAt(0);

            int maNganhIdx = -1, maToHopIdx = -1, tbKeysIdx = -1, doLechIdx = -1;
            int headerRowIdx = -1;

            for (int i = 0; i < 5; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String val = getStr(row, j);
                    if (val == null) continue;
                    val = val.toLowerCase().trim();
                    
                    if (val.equals("manganh")) maNganhIdx = j;
                    else if (val.equals("ma_to_hop")) maToHopIdx = j;
                    else if (val.equals("tb_keys")) tbKeysIdx = j;
                    else if (val.contains("độ lệch") || val.contains("do lech")) doLechIdx = j;
                }
                if (maNganhIdx != -1 && maToHopIdx != -1) {
                    headerRowIdx = i; break;
                }
            }

            if (headerRowIdx == -1) throw new Exception("Không tìm thấy cột MANGANH và MA_TO_HOP");

            for (int i = headerRowIdx + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String maNganh = getStr(row, maNganhIdx);
                String rawToHop = getStr(row, maToHopIdx);
                
                if (maNganh == null || rawToHop == null) continue;

                try {
                    String tbKeys = getStr(row, tbKeysIdx);
                    if (tbKeys == null || tbKeys.isBlank()) {
                        String tempMaTH = rawToHop.contains("(") ? rawToHop.substring(0, rawToHop.indexOf("(")).trim() : rawToHop;
                        tbKeys = maNganh + "_" + tempMaTH;
                    }

                    MajorCombination mc = existingMap.get(tbKeys);
                    if (mc == null) {
                        mc = new MajorCombination();
                        mc.setTbKeys(tbKeys);
                    }

                    mc.setMaNganh(maNganh);
                    
                    if (rawToHop.contains("(")) {
                        String maToHop = rawToHop.substring(0, rawToHop.indexOf("(")).trim();
                        mc.setMaToHop(maToHop);

                        String subjectsPart = rawToHop.substring(rawToHop.indexOf("(") + 1, rawToHop.indexOf(")")).trim();
                        String[] subjects = subjectsPart.split(","); 

                        resetSubjectFlags(mc);

                        if (subjects.length >= 1) {
                            String[] s1 = subjects[0].split("-");
                            if (s1.length == 2) {
                                mc.setThMon1(s1[0].trim());
                                mc.setHsMon1(Integer.parseInt(s1[1].trim()));
                                setSubjectFlag(mc, s1[0].trim());
                            }
                        }
                        if (subjects.length >= 2) {
                            String[] s2 = subjects[1].split("-");
                            if (s2.length == 2) {
                                mc.setThMon2(s2[0].trim());
                                mc.setHsMon2(Integer.parseInt(s2[1].trim()));
                                setSubjectFlag(mc, s2[0].trim());
                            }
                        }
                        if (subjects.length >= 3) {
                            String[] s3 = subjects[2].split("-");
                            if (s3.length == 2) {
                                mc.setThMon3(s3[0].trim());
                                mc.setHsMon3(Integer.parseInt(s3[1].trim()));
                                setSubjectFlag(mc, s3[0].trim());
                            }
                        }
                    } else {
                        mc.setMaToHop(rawToHop);
                    }

                    if (doLechIdx != -1) {
                        String doLechStr = getStr(row, doLechIdx);
                        if (doLechStr != null && !doLechStr.isBlank()) {
                            doLechStr = doLechStr.replace(",", ".");
                            mc.setDoLech(Double.parseDouble(doLechStr)); 
                        }
                    } else {
                        mc.setDoLech(0.0); 
                    }

                    listToImport.add(mc);
                    count++;
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": Lỗi phân tích dữ liệu - " + e.getMessage());
                }
            }
        }

        if (!listToImport.isEmpty()) {
            repository.importBatch(listToImport);
        }

        return new ImportResult(count, errors);
    }

    private String getStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String val = "";
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

    private void resetSubjectFlags(MajorCombination mc) {
        mc.setN1(false); mc.setToan(false); mc.setLy(false); mc.setHoa(false);
        mc.setSinh(false); mc.setVan(false); mc.setSu(false); mc.setDia(false);
        mc.setTiengAnh(false); mc.setKhac(false); mc.setKtpl(false);
    }

    private void setSubjectFlag(MajorCombination mc, String subjectCode) {
        if (subjectCode == null) return;
        switch (subjectCode.toUpperCase()) {
            case "N1": mc.setN1(true); break;
            case "TO": mc.setToan(true); break;
            case "LI": mc.setLy(true); break;
            case "HO": mc.setHoa(true); break;
            case "SI": mc.setSinh(true); break;
            case "VA": mc.setVan(true); break;
            case "SU": mc.setSu(true); break;
            case "DI": mc.setDia(true); break;
            case "TI": mc.setTiengAnh(true); break;
            case "KTPL": mc.setKtpl(true); break;
            default: 
                mc.setKhac(true); 
                break;
        }
    }
}