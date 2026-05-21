package com.tuyensinh;

import com.tuyensinh.entity.*;
import com.tuyensinh.repository.*;
import com.tuyensinh.service.AspirationService;
import com.tuyensinh.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestFormulas {

    public static void main(String[] args) {
        System.out.println("--- STARTING FORMULA TEST DATA SEEDING ---");
        
        try {
            seedTestData();
            
            System.out.println("Running Admission Calculation...");
            AspirationService service = new AspirationService();
            AspirationService.AdmissionResult result = service.runAdmission();
            
            System.out.println("Admission complete!");
            System.out.println("Total: " + result.getTotal());
            System.out.println("Passed: " + result.getPassed());
            System.out.println("Below Floor: " + result.getBelowFloor());
            
            System.out.println("\n--- TEST CASES VERIFICATION ---");
            verifyResults();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void seedTestData() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Clear existing data (Be careful with order due to FK)
            session.createQuery("DELETE FROM Aspiration").executeUpdate();
            session.createQuery("DELETE FROM BonusScore").executeUpdate();
            session.createQuery("DELETE FROM CandidateScore").executeUpdate();
            session.createQuery("DELETE FROM MajorCombination").executeUpdate();
            session.createQuery("DELETE FROM XtNganh").executeUpdate();
            session.createQuery("DELETE FROM XtToHopMon").executeUpdate();
            session.createQuery("DELETE FROM Candidate").executeUpdate();
            session.createQuery("DELETE FROM ScoreConversion").executeUpdate();
            session.createQuery("DELETE FROM EnglishCertificate").executeUpdate();

            // 1. Create Major (IT - Root A00, Floor 24.0, Quota 2)
            XtNganh it = new XtNganh();
            it.setManganh("IT01");
            it.setTennganh("Công nghệ thông tin");
            it.setNTohopgoc("A00");
            it.setNDiemsan(BigDecimal.valueOf(20.0));
            it.setNChitieu(10);
            session.persist(it);

            // 2. Create Combinations
            XtToHopMon a00 = new XtToHopMon(); a00.setMatohop("A00"); a00.setMon1("TO"); a00.setMon2("LI"); a00.setMon3("HO"); session.persist(a00);
            XtToHopMon a01 = new XtToHopMon(); a01.setMatohop("A01"); a01.setMon1("TO"); a01.setMon2("LI"); a01.setMon3("AN"); session.persist(a01);
            XtToHopMon vsat1 = new XtToHopMon(); vsat1.setMatohop("VSAT1"); vsat1.setMon1("TO"); vsat1.setMon2("LI"); vsat1.setMon3("HO"); session.persist(vsat1);

            // 3. Map Major to Combinations
            MajorCombination mc1 = new MajorCombination();
            mc1.setNganhId(it.getIdnganh()); mc1.setMaToHop("A00"); mc1.setHsMon1(1); mc1.setHsMon2(1); mc1.setHsMon3(1); session.persist(mc1);
            
            MajorCombination mc2 = new MajorCombination();
            mc2.setNganhId(it.getIdnganh()); mc2.setMaToHop("A01"); mc2.setHsMon1(1); mc2.setHsMon2(1); mc2.setHsMon3(1); session.persist(mc2);

            MajorCombination mc3 = new MajorCombination();
            mc3.setNganhId(it.getIdnganh()); mc3.setMaToHop("VSAT1"); mc3.setHsMon1(1); mc3.setHsMon2(1); mc3.setHsMon3(1); session.persist(mc3);

            // 4. Create Conversion (VSAT -> THPT)
            // 400 -> 5.0, 600 -> 9.0
            ScoreConversion sc = new ScoreConversion();
            sc.setPhuongThuc("VSAT"); sc.setMon("TO"); sc.setDiemA(400.0); sc.setDiemB(600.0); sc.setDiemC(5.0); sc.setDiemD(9.0); session.persist(sc);
            ScoreConversion sc2 = new ScoreConversion();
            sc2.setPhuongThuc("VSAT"); sc2.setMon("LI"); sc2.setDiemA(400.0); sc2.setDiemB(600.0); sc2.setDiemC(5.0); sc2.setDiemD(9.0); session.persist(sc2);
            ScoreConversion sc3 = new ScoreConversion();
            sc3.setPhuongThuc("VSAT"); sc3.setMon("HO"); sc3.setDiemA(400.0); sc3.setDiemB(600.0); sc3.setDiemC(5.0); sc3.setDiemD(9.0); session.persist(sc3);

            // 5. Test Candidates
            
            // Candidate 1: Scaling Formula (Total >= 22.5)
            // ĐTHGXT = 24.0, ĐC = 1.0 -> Sum = 25.0. MĐƯT = 2.0. Expected ĐƯT = (30-25)/7.5 * 2 = 1.33. Final = 26.33
            Candidate c1 = createCandidate("C001", "Nguyen Van A"); session.persist(c1);
            createScore(session, "C001", "THPT", 8.0, 8.0, 8.0); // A00 = 24.0
            createBonus(session, "C001", null, 1.0, 2.0); // ĐC = 1.0, MĐƯT = 2.0
            createAspiration(session, "C001", it.getIdnganh(), 1, "THPT", "A00");

            // Candidate 2: Deviation Test (A01 to A00)
            // A01 = 8+8+8 = 24.0. Deviation = -0.69. ĐTHGXT = 24 - (-0.69) = 24.69. ĐC=0, ĐƯT=0. Final = 24.69
            Candidate c2 = createCandidate("C002", "Tran Thi B"); session.persist(c2);
            createScore(session, "C002", "THPT", 8.0, 8.0, 8.0, 8.0); // TO=8, LI=8, AN=8 -> A01 = 24.0
            createAspiration(session, "C002", it.getIdnganh(), 1, "THPT", "A01");

            // Candidate 3: Interpolation Test (VSAT)
            // TO=500 -> 7.0, LI=500 -> 7.0, HO=500 -> 7.0. Total = 7+7+7 = 21.0. Final = 21.0
            Candidate c3 = createCandidate("C003", "Le Van C"); session.persist(c3);
            CandidateScore s3 = new CandidateScore();
            s3.setCccd("C003"); s3.setDPhuongthuc("VSAT"); s3.setTo(BigDecimal.valueOf(500)); s3.setLi(BigDecimal.valueOf(500)); s3.setHo(BigDecimal.valueOf(500));
            session.persist(s3);
            createAspiration(session, "C003", it.getIdnganh(), 1, "VSAT", "VSAT1");

            // Candidate 4: English Certificate Conversion
            // IELTS Level 2 -> English score = 9.0. THPT: TO=8, LI=8, AN=5. A01 = (8+8+9)/3*3 = 25.0. Final = 25.0
            Candidate c4 = createCandidate("C004", "Hoang Thi D"); session.persist(c4);
            createScore(session, "C004", "THPT", 8.0, 8.0, 0.0, 5.0); // TO=8, LI=8, AN=5
            EnglishCertificate ec = new EnglishCertificate();
            ec.setCccd("C004"); ec.setLoaiCc("IELTS"); ec.setDiemSo(BigDecimal.valueOf(6.5)); // Level 2
            session.persist(ec);
            createAspiration(session, "C004", it.getIdnganh(), 1, "THPT", "A01");

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    private static void verifyResults() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Aspiration> results = session.createQuery("FROM Aspiration ORDER BY cccd", Aspiration.class).list();
            for (Aspiration a : results) {
                System.out.printf("Candidate: %s | Comb: %s | ĐTHGXT: %s | ĐC: %s | ĐƯT: %s | ĐXT: %s | KQ: %s%n",
                    a.getCccd(), a.getToHop(), a.getDiemThxt(), a.getDiemCc(), a.getDiemUtqd(), a.getDiemXetTuyen(), a.getKetQua());
            }
        }
    }

    private static Candidate createCandidate(String cccd, String name) {
        Candidate c = new Candidate();
        c.setCccd(cccd);
        c.setHo(name.split(" ")[0]);
        c.setTen(name.substring(name.indexOf(" ") + 1));
        c.setNamTuyenSinh(2025);
        return c;
    }

    private static void createScore(Session s, String cccd, String method, double to, double li, double ho) {
        createScore(s, cccd, method, to, li, ho, 0);
    }

    private static void createScore(Session session, String cccd, String method, double to, double li, double ho, double an) {
        CandidateScore s = new CandidateScore();
        s.setCccd(cccd);
        s.setDPhuongthuc(method);
        s.setTo(BigDecimal.valueOf(to));
        s.setLi(BigDecimal.valueOf(li));
        s.setHo(BigDecimal.valueOf(ho));
        s.setN1Thi(BigDecimal.valueOf(an));
        session.persist(s);
    }

    private static void createBonus(Session s, String cccd, String major, double dc, double mdut) {
        BonusScore b = new BonusScore();
        b.setCccd(cccd);
        b.setNganhId(major == null ? null : Integer.parseInt(major));
        b.setDiemCc(BigDecimal.valueOf(dc));
        b.setDiemUtxt(BigDecimal.valueOf(mdut));
        b.setMaToHop(""); b.setPhuongThuc("");
        s.persist(b);
    }

    private static void createAspiration(Session s, String cccd, Integer majorId, int order, String method, String comb) {
        Aspiration a = new Aspiration();
        a.setCccd(cccd);
        a.setNganhId(majorId);
        a.setThuTu(order);
        a.setPhuongThuc(method);
        a.setToHop(comb);
        s.persist(a);
    }
}
