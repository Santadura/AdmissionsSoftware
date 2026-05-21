package com.tuyensinh.repository;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.CandidateScore;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class CandidateScoreRepository {

    // ================= SAVE =================

    public void save(CandidateScore score) {

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil
                    .getSessionFactory()
                    .openSession();

            transaction = session.beginTransaction();

            session.save(score);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Lỗi thêm điểm: " + e.getMessage(),
                    e
            );

        } finally {

            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public boolean existsByCccdAndLoai(String cccd, String loai) {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM CandidateScore " +
                            "WHERE cccd = :cccd " +
                            "AND dPhuongthuc = :loai",
                    Long.class
            );

            query.setParameter("cccd", cccd);
            query.setParameter("loai", loai);

            return query.uniqueResult() > 0;
        }
    }

    // ================= UPDATE =================

    public void update(CandidateScore score) {

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil
                    .getSessionFactory()
                    .openSession();

            transaction = session.beginTransaction();

            session.merge(score);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Lỗi cập nhật điểm: " + e.getMessage(),
                    e
            );

        } finally {

            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    // ================= DELETE =================

    public void delete(Integer id) {

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil
                    .getSessionFactory()
                    .openSession();

            transaction = session.beginTransaction();

            CandidateScore score = session.get(
                    CandidateScore.class,
                    id
            );

            if (score != null) {
                session.remove(score);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Lỗi xóa điểm: " + e.getMessage(),
                    e
            );

        } finally {

            if (session != null && session.isOpen()) {
                session.close();
            }
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

    //=============== IMPORT ================
    public CandidateScore findByCccdAndLoai(String cccd, String loai) {

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            Query<CandidateScore> query = session.createQuery(
                    "FROM CandidateScore " +
                            "WHERE cccd = :cccd " +
                            "AND dPhuongthuc = :loai",
                    CandidateScore.class
            );

            query.setParameter("cccd", cccd);
            query.setParameter("loai", loai);

            return query.uniqueResult();
        }
    }

    public void saveOrUpdateByCccdAndLoai(CandidateScore imported) {

        Transaction transaction = null;

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            transaction = session.beginTransaction();

            Query<CandidateScore> query = session.createQuery(
                    "FROM CandidateScore " +
                            "WHERE cccd = :cccd " +
                            "AND dPhuongthuc = :loai",
                    CandidateScore.class
            );

            query.setParameter("cccd", imported.getCccd());
            query.setParameter("loai", imported.getDPhuongthuc());

            CandidateScore existing = query.uniqueResult();

            if (existing == null) {
                session.save(imported);
            } else {
                imported.setIddiemthi(existing.getIddiemthi());
                session.merge(imported);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Lỗi lưu điểm: " + e.getMessage(), e);
        }
    }
}
