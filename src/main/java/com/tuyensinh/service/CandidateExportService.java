package com.tuyensinh.service;

import com.tuyensinh.entity.Candidate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class CandidateExportService {

    public void exportToExcel(File file, List<Candidate> candidates) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Đặt tên sheet giống file input
            Sheet sheet = workbook.createSheet("Sheet1");

            // Tạo header GIỐNG HỆT file dsthisinh1.xlsx
            Row header = sheet.createRow(0);
            String[] columns = {
                "STT", "CCCD", "Họ Tên", "Ngày sinh", "Giới tính",
                "ĐTƯT", "KVƯT", "TO", "VA", "LI", "HO", "SI", "SU", "DI",
                "GDCD", "NN", "Mã môn NN", "KTPL", "TI", "CNCN", "CNNN",
                "Chương trình học", "NK1", "NK2", "NK3", "NK4", "NK5", "NK6",
                "NK7", "NK8", "NK9", "NK10", "Điểm xét tốt nghiệp", "Dân tộc",
                "Mã dân tộc", "Nơi sinh"
            };
            
            CellStyle headerStyle = getHeaderStyle(workbook);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Đổ dữ liệu
            int rowNum = 1;
            for (Candidate c : candidates) {
                Row row = sheet.createRow(rowNum);
                
                row.createCell(0).setCellValue(rowNum);  // STT
                row.createCell(1).setCellValue(c.getCccd() != null ? c.getCccd() : "");
                row.createCell(2).setCellValue(c.getHoTen());
                row.createCell(3).setCellValue(c.getNgaySinh() != null ? c.getNgaySinh() : "");
                row.createCell(4).setCellValue(c.getGioiTinh() != null ? c.getGioiTinh() : "");
                row.createCell(5).setCellValue(c.getDoiTuong() != null ? c.getDoiTuong() : "");
                row.createCell(6).setCellValue(c.getKhuVuc() != null ? c.getKhuVuc() : "");
                
                // Các cột điểm (7 đến 35) để trống - có thể lấy từ bảng điểm sau
                for (int i = 7; i <= 35; i++) {
                    row.createCell(i).setCellValue("");
                }
                
                row.createCell(36).setCellValue(c.getNoiSinh() != null ? c.getNoiSinh() : "");
                
                rowNum++;
            }

            // Auto resize các cột
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Ghi ra file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
}