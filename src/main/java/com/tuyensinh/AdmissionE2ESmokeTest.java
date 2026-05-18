package com.tuyensinh;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.service.AspirationService;
import com.tuyensinh.service.AspirationService.AdmissionResult;

public class AdmissionE2ESmokeTest {

    public static void main(String[] args) {
        try {
            AdmissionResult result = new AspirationService().runAdmission();
            System.out.println("Da chay xet tuyen. Tong NV: " + result.getTotal()
                    + ", trung tuyen: " + result.getPassed()
                    + ", khong trung tuyen: " + result.getFailed()
                    + ", duoi san: " + result.getBelowFloor()
                    + ", chua co diem: " + result.getMissingScore()
                    + ", chua cau hinh nganh: " + result.getMissingMajorConfig());

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Map<String, Integer> resultCounts = loadResultCounts(session);
                assertCount(resultCounts, "trungtuyen", 5);
                assertCount(resultCounts, "khongtrungtuyen", 2);
                assertCount(resultCounts, "duoisan", 1);
                assertCount(resultCounts, "chuaxet", 1);
                assertCount(resultCounts, "chuacauhinh", 1);

                assertAspiration(session, "900000000001", "7480201", 1, "trungtuyen", "1.00", "27.00000");
                assertAspiration(session, "900000000002", "7480201", 1, "trungtuyen", "0.00", "25.00000");
                assertAspiration(session, "900000000003", "7480201", 1, "khongtrungtuyen", "0.20", "24.70000");
                assertAspiration(session, "900000000004", "7340101", 1, "duoisan", "0.30", "20.80000");
                assertAspiration(session, "900000000005", "7340101", 1, "trungtuyen", "0.50", "22.30000");
                assertAspiration(session, "900000000006", "7480201", 1, "khongtrungtuyen", "0.00", "24.20000");
                assertAspiration(session, "900000000006", "7220201", 2, "trungtuyen", "0.00", "22.50000");
                assertAspiration(session, "900000000007", "9999999", 1, "chuacauhinh", "0.00", "28.00000");
                assertAspiration(session, "900000000008", "7480201", 1, "chuaxet", "0.00", "0.00000");
                assertAspiration(session, "900000000009", "7340101", 1, "trungtuyen", "0.00", "23.00000");
            }

            System.out.println("E2E admission smoke test PASSED.");
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static Map<String, Integer> loadResultCounts(Session session) {
        List<Object[]> rows = session.createNativeQuery("""
                SELECT nv_ketqua, COUNT(*)
                FROM xt_nguyenvongxettuyen
                WHERE nn_cccd LIKE '9000000000%'
                GROUP BY nv_ketqua
                """, Object[].class).list();

        Map<String, Integer> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(row[0].toString(), ((Number) row[1]).intValue());
        }
        return result;
    }

    private static void assertCount(Map<String, Integer> counts, String key, int expected) {
        int actual = counts.getOrDefault(key, 0);
        if (actual != expected) {
            throw new IllegalStateException("Ket qua " + key + " sai. Expected " + expected + ", actual " + actual);
        }
    }

    private static void assertAspiration(
            Session session,
            String cccd,
            String maNganh,
            int thuTu,
            String expectedResult,
            String expectedBonus,
            String expectedAdmissionScore) {
        Object[] row = session.createNativeQuery("""
                SELECT diem_cong, diem_xettuyen, nv_ketqua
                FROM xt_nguyenvongxettuyen
                WHERE nn_cccd = :cccd
                  AND nv_manganh = :maNganh
                  AND nv_tt = :thuTu
                """, Object[].class)
                .setParameter("cccd", cccd)
                .setParameter("maNganh", maNganh)
                .setParameter("thuTu", thuTu)
                .uniqueResult();

        if (row == null) {
            throw new IllegalStateException("Khong tim thay nguyen vong e2e: " + cccd + " - " + maNganh);
        }

        assertDecimal("diem_cong " + cccd + " " + maNganh, row[0], expectedBonus);
        assertDecimal("diem_xettuyen " + cccd + " " + maNganh, row[1], expectedAdmissionScore);
        String actualResult = row[2] == null ? null : row[2].toString();
        if (!expectedResult.equals(actualResult)) {
            throw new IllegalStateException("Ket qua " + cccd + " " + maNganh
                    + " sai. Expected " + expectedResult + ", actual " + actualResult);
        }
    }

    private static void assertDecimal(String label, Object actualValue, String expectedValue) {
        BigDecimal actual = actualValue == null ? BigDecimal.ZERO : (BigDecimal) actualValue;
        BigDecimal expected = new BigDecimal(expectedValue);
        if (actual.compareTo(expected) != 0) {
            throw new IllegalStateException(label + " sai. Expected " + expected + ", actual " + actual);
        }
    }
}
