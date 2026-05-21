package com.tuyensinh.service;

import com.tuyensinh.entity.MajorCombination;
import com.tuyensinh.repository.MajorCombinationRepository;
import java.util.List;

public class MajorCombinationService {
    private MajorCombinationRepository repository;

    public MajorCombinationService() {
        this.repository = new MajorCombinationRepository();
    }

    public List<MajorCombination> getAll() {
        return repository.findAll();
    }

    public List<Object[]> getAllWithMajor() {
        return repository.findAllWithMajor();
    }

    public List<Object[]> search(String term) {
        return repository.searchWithMajor(term);
    }

    public void save(MajorCombination mc) {
        if (mc.getNganhId() != null && mc.getMaToHop() != null) {
            mc.setTbKeys(mc.getNganhId() + "_" + mc.getMaToHop());
        }
        repository.saveOrUpdate(mc);
    }

    public List<MajorCombination> getByNganhId(Integer nganhId) {
        return repository.findByNganhId(nganhId);
    }

    public void delete(int id) {
        repository.delete(id);
    }

    public MajorCombination getById(int id) {
        return repository.findById(id);
    }

    public static class ImportResult {
        public final int successCount;
        public final List<String> errors;
        public ImportResult(int s, List<String> e) { successCount = s; errors = e; }
    }

    public ImportResult importFromExcel(java.io.File file) throws Exception {
        List<String> errors = new java.util.ArrayList<>();
        int count = 0;
        
        // Map quy đổi mã môn sang tên đầy đủ (đồng bộ với ToHopMonService)
        java.util.Map<String, String> monMap = new java.util.HashMap<>();
        monMap.put("TO", "Toán"); monMap.put("TOAN", "Toán"); monMap.put("T", "Toán");
        monMap.put("VA", "Văn"); monMap.put("VAN", "Văn"); monMap.put("V", "Văn");
        monMap.put("LI", "Lý"); monMap.put("LY", "Lý"); monMap.put("L", "Lý");
        monMap.put("HO", "Hóa"); monMap.put("HOA", "Hóa"); monMap.put("H", "Hóa");
        monMap.put("SI", "Sinh"); monMap.put("SINH", "Sinh"); monMap.put("S", "Sinh");
        monMap.put("SU", "Sử");
        monMap.put("DI", "Địa");
        monMap.put("AN", "Anh"); monMap.put("ANH", "Anh"); monMap.put("A", "Anh");
        monMap.put("GDCD", "GDCD"); monMap.put("KTPL", "KTPL");
        monMap.put("TI", "T.Anh");
        monMap.put("NL1", "N.Lực 1");
        monMap.put("NK1", "N.Khiếu 1"); monMap.put("NK2", "N.Khiếu 2");
        monMap.put("NK3", "N.Khiếu 3"); monMap.put("NK4", "N.Khiếu 4");
        monMap.put("CNCN", "CN Công nghiệp"); monMap.put("CNNN", "CN Nông nghiệp");

        com.tuyensinh.repository.NganhRepository nganhRepo = new com.tuyensinh.repository.NganhRepository();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("([A-Z0-9]+)-([\\d.]+)");

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            int rowNum = 0;
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue;
                if (row == null) continue;

                String maNganh = getStr(row, 1); // Cột B: Mã ngành
                String maFull = getStr(row, 3);  // Cột D: B03(TO-3,VA-3,SI-1)
                String maTohop = getStr(row, 5); // Cột F: B03
                String doLechStr = getStr(row, 7); // Giả sử cột H là điểm lệch

                if (maNganh == null || maFull == null || maTohop == null) continue;

                try {
                    com.tuyensinh.entity.XtNganh nganh = nganhRepo.findByManganh(maNganh);
                    if (nganh == null) {
                        errors.add("Dòng " + rowNum + ": Không tìm thấy ngành " + maNganh);
                        continue;
                    }

                    MajorCombination mc = new MajorCombination();
                    mc.setNganhId(nganh.getIdnganh());
                    mc.setMaToHop(maTohop);
                    
                    if (doLechStr != null) {
                        try {
                            mc.setDoLech(Double.parseDouble(doLechStr.replaceAll("[^\\d.-]", "")));
                        } catch (Exception e) {
                            mc.setDoLech(0.0);
                        }
                    } else {
                        mc.setDoLech(0.0);
                    }

                    // Parse subjects and weights: (TO-3,VA-3,SI-1)
                    int start = maFull.indexOf('(');
                    int end = maFull.lastIndexOf(')');
                    if (start != -1 && end != -1) {
                        String inside = maFull.substring(start + 1, end);
                        java.util.regex.Matcher m = p.matcher(inside);
                        int idx = 1;
                        while (m.find() && idx <= 3) {
                            String subjectCode = m.group(1).toUpperCase();
                            String subjectName = monMap.getOrDefault(subjectCode, subjectCode);
                            int weight = (int) Double.parseDouble(m.group(2));
                            
                            if (idx == 1) { mc.setThMon1(subjectName); mc.setHsMon1(weight); }
                            else if (idx == 2) { mc.setThMon2(subjectName); mc.setHsMon2(weight); }
                            else if (idx == 3) { mc.setThMon3(subjectName); mc.setHsMon3(weight); }
                            idx++;
                        }
                    }

                    save(mc);
                    count++;
                } catch (Exception e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }
        return new ImportResult(count, errors);
    }

    private String getStr(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                return (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            default: return null;
        }
    }
}
