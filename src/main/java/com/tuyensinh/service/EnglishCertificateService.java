package com.tuyensinh.service;

import com.tuyensinh.entity.EnglishCertificate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class EnglishCertificateService {

    /**
     * Xác định mức quy đổi (1, 2, 3) dựa trên loại chứng chỉ và điểm số.
     * Trả về 0 nếu không đạt mức tối thiểu.
     */
    public int getLevel(String type, double score) {
        if (type == null) return 0;
        String t = type.toUpperCase().trim();

        if (t.contains("IELTS")) {
            if (score >= 7.0) return 3;
            if (score >= 5.5) return 2;
            if (score >= 4.0) return 1;
        } else if (t.contains("TOEFL ITP")) {
            if (score >= 627) return 3;
            if (score >= 500) return 2;
            if (score >= 450) return 1;
        } else if (t.contains("TOEFL IBT")) {
            if (score >= 94) return 3;
            if (score >= 46) return 2;
            if (score >= 30) return 1;
        } else if (t.contains("PTE")) {
            if (score >= 76) return 3;
            if (score >= 59) return 2;
            if (score >= 43) return 1;
        } else if (t.contains("LINGUASKILL")) {
            if (score >= 180) return 3;
            if (score >= 160) return 2;
            if (score >= 140) return 1;
        } else if (t.contains("VSTEP")) {
            if (score >= 5) return 3;
            if (score >= 4) return 2;
            if (score >= 3) return 1;
        } else if (t.contains("TOEIC")) {
            // Giả sử score nhập vào là điểm trung bình hoặc điểm quy đổi tổng quát của 4 kỹ năng
            // (Cần xử lý chi tiết hơn nếu nhập từng kỹ năng)
            if (score >= 490) return 3;
            if (score >= 400) return 2;
            if (score >= 275) return 1;
        }
        
        // Aptis (xử lý theo bậc chữ)
        // Lưu ý: Nếu nhập score dạng số, cần quy ước B1=1, B2=2, C/C1=3
        if (t.contains("APTIS")) {
            if (score >= 3) return 3; // C hoặc C1
            if (score >= 2) return 2; // B2
            if (score >= 1) return 1; // B1
        }

        return 0;
    }

    /**
     * Lấy điểm cộng dựa trên mức và phương thức xét tuyển.
     * Quy về thang điểm 30 cho tất cả các phương thức để cộng trực tiếp vào ĐTHGXT.
     */
    public BigDecimal getBonusPoints(int level, String method) {
        if (level <= 0) return BigDecimal.ZERO;
        // Tất cả các phương thức đều quy về thang 30: Mức 1 = 1.0, Mức 2 = 1.5, Mức 3 = 2.0
        switch (level) {
            case 1: return new BigDecimal("1.0");
            case 2: return new BigDecimal("1.5");
            case 3: return new BigDecimal("2.0");
            default: return BigDecimal.ZERO;
        }
    }

    /**
     * Lấy điểm quy đổi sang môn Tiếng Anh (Kỳ thi THPT).
     */
    public BigDecimal getEnglishSubjectScore(int level) {
        switch (level) {
            case 1: return new BigDecimal("8.0");
            case 2: return new BigDecimal("9.0");
            case 3: return new BigDecimal("10.0");
            default: return null;
        }
    }
}
