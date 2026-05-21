package com.tuyensinh.repository;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.CandidateScore;

public class CandidateScoreRepository {


    // ================= SAVE =================

    public void save(CandidateScore score) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            CandidateScore existing = findByCccdAndPhuongThuc(session, score.getCccd(), score.getDPhuongthuc());
            if (existing == null) {
                session.persist(score);
            } else {
                updateFields(existing, score);
                session.merge(existing);
            }
            transaction.commit();
                } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw new RuntimeException("CCCD thí sinh không tồn tại trong hệ thống (foreign key violation)!");
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Lỗi thêm điểm: " + e.getMessage(), e);
        } finally {
            if (session != null) session.close();
        }
    }

    public void update(CandidateScore score) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            CandidateScore existing = findByCccdAndPhuongThuc(session, score.getCccd(), score.getDPhuongthuc());
            if (existing != null && !existing.getIddiemthi().equals(score.getIddiemthi())) {
                throw new RuntimeException("Điểm của thí sinh " + score.getCccd() + " cho phương thức thi " + score.getDPhuongthuc() + " đã tồn tại!");
            }
            session.merge(score);
            transaction.commit();
                } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw new RuntimeException("CCCD thí sinh không tồn tại trong hệ thống (foreign key violation)!");
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Lỗi cập nhật điểm: " + e.getMessage(), e);
        } finally {
            if (session != null) session.close();
        }
    }

    public void saveAll(List<CandidateScore> scores) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            int count = 0;
            for (CandidateScore score : scores) {
                CandidateScore existing = findByCccdAndPhuongThuc(session, score.getCccd(), score.getDPhuongthuc());
                if (existing == null) {
                    session.persist(score);
                } else {
                    updateFields(existing, score);
                    session.merge(existing);
                }
                
                if (++count % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
                } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw new RuntimeException("CCCD thí sinh không tồn tại trong hệ thống (foreign key violation)!");
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Lỗi lưu điểm: " + e.getMessage(), e);
        } finally {
            if (session != null) session.close();
        }
    }

    private CandidateScore findByCccdAndPhuongThuc(Session session, String cccd, String phuongThuc) {
        Query<CandidateScore> query = session.createQuery(
            "FROM CandidateScore WHERE cccd = :cccd AND dPhuongthuc = :pt", CandidateScore.class);
        query.setParameter("cccd", cccd);
        query.setParameter("pt", phuongThuc);
        return query.uniqueResult();
    }

    private void updateFields(CandidateScore target, CandidateScore source) {
        if (source.getSobaodanh() != null) target.setSobaodanh(source.getSobaodanh());
        if (source.getTo() != null) target.setTo(source.getTo());
        if (source.getLi() != null) target.setLi(source.getLi());
        if (source.getHo() != null) target.setHo(source.getHo());
        if (source.getSi() != null) target.setSi(source.getSi());
        if (source.getSu() != null) target.setSu(source.getSu());
        if (source.getDi() != null) target.setDi(source.getDi());
        if (source.getVa() != null) target.setVa(source.getVa());
        if (source.getN1Thi() != null) target.setN1Thi(source.getN1Thi());
        if (source.getN1Cc() != null) target.setN1Cc(source.getN1Cc());
        if (source.getCncn() != null) target.setCncn(source.getCncn());
        if (source.getCnnn() != null) target.setCnnn(source.getCnnn());
        if (source.getTi() != null) target.setTi(source.getTi());
        if (source.getKtpl() != null) target.setKtpl(source.getKtpl());
        if (source.getNl1() != null) target.setNl1(source.getNl1());
        if (source.getNk1() != null) target.setNk1(source.getNk1());
        if (source.getNk2() != null) target.setNk2(source.getNk2());
        if (source.getNk3() != null) target.setNk3(source.getNk3());
        if (source.getNk4() != null) target.setNk4(source.getNk4());
    }

    public List<CandidateScore> findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<CandidateScore> query = session.createQuery(
                "FROM CandidateScore WHERE cccd = :cccd ORDER BY dPhuongthuc", CandidateScore.class);
            query.setParameter("cccd", cccd);
            return query.list();
        }
    }

    // ================= DELETE =================

    public void delete(Integer id) {

        Transaction transaction = null;

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            transaction = session.beginTransaction();

            CandidateScore score = session.get(
                    CandidateScore.class,
                    id
            );

            if (score != null) {
                session.delete(score);
            }

            transaction.commit();

                } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw new RuntimeException("CCCD thí sinh không tồn tại trong hệ thống (foreign key violation)!");
        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Lỗi xóa điểm: " + e.getMessage()
            );
        }
    }

    // ================= FIND BY ID =================

    public CandidateScore findById(Integer id) {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            return session.get(CandidateScore.class, id);
        }
    }

    // ================= FIND ALL =================

    public List<CandidateScore> findAll() {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            Query<CandidateScore> query = session.createQuery(
                    "FROM CandidateScore ORDER BY iddiemthi ASC",
                    CandidateScore.class
            );

            return query.list();
        }
    }

    // ================= FIND BY LOAI =================

    public List<CandidateScore> findByLoai(String loai) {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            Query<CandidateScore> query = session.createQuery(
                    "FROM CandidateScore WHERE dPhuongthuc = :loai " +
                            "ORDER BY iddiemthi ASC",
                    CandidateScore.class
            );

            query.setParameter("loai", loai);

            return query.list();
        }
    }

    // ================= SEARCH =================

    public List<CandidateScore> search(String keyword) {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            String hql =
                    "FROM CandidateScore " +
                            "WHERE cccd LIKE :kw " +
                            "OR sobaodanh LIKE :kw " +
                            "ORDER BY iddiemthi ASC";

            Query<CandidateScore> query = session.createQuery(
                    hql,
                    CandidateScore.class
            );

            query.setParameter("kw", "%" + keyword + "%");

            return query.list();
        }
    }

    // ================= SEARCH + LOAI =================

    public List<CandidateScore> searchByLoai(
            String keyword,
            String loai
    ) {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            String hql =
                    "FROM CandidateScore " +
                            "WHERE dPhuongthuc = :loai " +
                            "AND (" +
                            "cccd LIKE :kw " +
                            "OR sobaodanh LIKE :kw" +
                            ") " +
                            "ORDER BY iddiemthi ASC";

            Query<CandidateScore> query = session.createQuery(
                    hql,
                    CandidateScore.class
            );

            query.setParameter("loai", loai);
            query.setParameter("kw", "%" + keyword + "%");

            return query.list();
        }
    }

    // ================= COUNT =================

    public long count() {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM CandidateScore",
                    Long.class
            );

            return query.uniqueResult();
        }
    }

    // ================= COUNT BY LOAI =================

    public long countByLoai(String loai) {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM CandidateScore " +
                            "WHERE dPhuongthuc = :loai",
                    Long.class
            );

            query.setParameter("loai", loai);

            return query.uniqueResult();
        }
    }

// ================= THỐNG KÊ THEO LOẠI, MÔN =================

    @SuppressWarnings("unchecked")
    public List<Object[]> statisticByLoaiAndMon() {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            String sql =
                    "SELECT d_phuongthuc, mon, so_luong, diem_tb, diem_min, diem_max " +
                            "FROM ( " +

                            "SELECT d_phuongthuc, 'TO' mon, COUNT(`TO`) so_luong, AVG(`TO`) diem_tb, MIN(`TO`) diem_min, MAX(`TO`) diem_max " +
                            "FROM xt_diemthixettuyen WHERE `TO` IS NOT NULL GROUP BY d_phuongthuc " +

                            "UNION ALL " +

                            "SELECT d_phuongthuc, 'LI' mon, COUNT(`LI`), AVG(`LI`), MIN(`LI`), MAX(`LI`) " +
                            "FROM xt_diemthixettuyen WHERE `LI` IS NOT NULL GROUP BY d_phuongthuc " +

                            "UNION ALL " +

                            "SELECT d_phuongthuc, 'HO' mon, COUNT(`HO`), AVG(`HO`), MIN(`HO`), MAX(`HO`) " +
                            "FROM xt_diemthixettuyen WHERE `HO` IS NOT NULL GROUP BY d_phuongthuc " +

                            "UNION ALL " +

                            "SELECT d_phuongthuc, 'SI' mon, COUNT(`SI`), AVG(`SI`), MIN(`SI`), MAX(`SI`) " +
                            "FROM xt_diemthixettuyen WHERE `SI` IS NOT NULL GROUP BY d_phuongthuc " +

                            "UNION ALL " +

                            "SELECT d_phuongthuc, 'SU' mon, COUNT(`SU`), AVG(`SU`), MIN(`SU`), MAX(`SU`) " +
                            "FROM xt_diemthixettuyen WHERE `SU` IS NOT NULL GROUP BY d_phuongthuc " +

                            "UNION ALL " +

                            "SELECT d_phuongthuc, 'DI' mon, COUNT(`DI`), AVG(`DI`), MIN(`DI`), MAX(`DI`) " +
                            "FROM xt_diemthixettuyen WHERE `DI` IS NOT NULL GROUP BY d_phuongthuc " +

                            "UNION ALL " +

                            "SELECT d_phuongthuc, 'VA' mon, COUNT(`VA`), AVG(`VA`), MIN(`VA`), MAX(`VA`) " +
                            "FROM xt_diemthixettuyen WHERE `VA` IS NOT NULL GROUP BY d_phuongthuc " +

                            "UNION ALL " +

                            "SELECT d_phuongthuc, 'N1_THI' mon, COUNT(`N1_THI`), AVG(`N1_THI`), MIN(`N1_THI`), MAX(`N1_THI`) " +
                            "FROM xt_diemthixettuyen WHERE `N1_THI` IS NOT NULL GROUP BY d_phuongthuc " +

                            "UNION ALL " +

                            "SELECT d_phuongthuc, 'NL1' mon, COUNT(`NL1`), AVG(`NL1`), MIN(`NL1`), MAX(`NL1`) " +
                            "FROM xt_diemthixettuyen WHERE `NL1` IS NOT NULL GROUP BY d_phuongthuc " +

                            ") t " +
                            "ORDER BY d_phuongthuc, mon";

            return session.createNativeQuery(sql).list();
        }
    }
}