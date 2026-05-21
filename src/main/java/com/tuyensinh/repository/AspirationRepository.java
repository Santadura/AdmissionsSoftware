package com.tuyensinh.repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.Aspiration;

public class AspirationRepository {

    public List<Object[]> findAllWithCandidate(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT a, c.ho, c.ten, n.manganh, n.tennganh
                    FROM Aspiration a
                    LEFT JOIN Candidate c ON a.cccd = c.cccd
                    LEFT JOIN XtNganh n ON a.nganhId = n.idnganh
                    """;
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String term = "%" + searchTerm.trim().toLowerCase() + "%";
                hql += """
                    WHERE lower(a.cccd) LIKE :term
                       OR lower(n.manganh) LIKE :term
                       OR lower(a.phuongThuc) LIKE :term
                       OR lower(a.toHop) LIKE :term
                       OR lower(a.ketQua) LIKE :term
                       OR lower(c.ho) LIKE :term
                       OR lower(c.ten) LIKE :term
                       OR lower(concat(c.ho, ' ', c.ten)) LIKE :term
                    """;
                hql += " ORDER BY a.id";
                return session.createQuery(hql, Object[].class)
                        .setParameter("term", term)
                        .list();
            }
            
            hql += " ORDER BY a.id";
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public List<Object[]> findAllSuccessfulWithCandidate() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT n.manganh, a.cccd, c.ho, c.ten, a.toHop, a.diemThxt, a.diemUtqd, a.diemCong, a.diemXetTuyen, a.phuongThuc, a.thuTu
                    FROM Aspiration a
                    LEFT JOIN Candidate c ON a.cccd = c.cccd
                    LEFT JOIN XtNganh n ON a.nganhId = n.idnganh
                    WHERE a.ketQua = 'trungtuyen'
                    ORDER BY n.manganh, a.diemXetTuyen DESC
                    """;
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public List<Object[]> countSuccessfulByMethodAndMajor() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT n.manganh, a.phuongThuc, COUNT(a.id)
                    FROM Aspiration a
                    LEFT JOIN XtNganh n ON a.nganhId = n.idnganh
                    WHERE a.ketQua = 'trungtuyen'
                    GROUP BY n.manganh, a.phuongThuc
                    ORDER BY n.manganh, a.phuongThuc
                    """;
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public List<Aspiration> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Aspiration a ORDER BY a.id", Aspiration.class).list();
        }
    }

    public Aspiration findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Aspiration.class, id);
        }
    }

    public List<Aspiration> findAllByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Aspiration WHERE cccd = :cccd", Aspiration.class)
                    .setParameter("cccd", cccd)
                    .list();
        }
    }

    public void save(Aspiration aspiration) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(aspiration);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Khong the luu nguyen vong: " + e.getMessage(), e);
        }
    }

    public void update(Aspiration aspiration) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(aspiration);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Khong the cap nhat nguyen vong: " + e.getMessage(), e);
        }
    }

    public void delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Aspiration aspiration = session.get(Aspiration.class, id);
            if (aspiration != null) {
                session.remove(aspiration);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Khong the xoa nguyen vong: " + e.getMessage(), e);
        }
    }

    public void saveAll(List<Aspiration> aspirations) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int count = 0;
            for (Aspiration aspiration : aspirations) {
                session.merge(aspiration);
                if (++count % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Khong the luu ket qua xet tuyen: " + e.getMessage(), e);
        }
    }

    public Map<Integer, BigDecimal[]> findMajorFloors() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT idnganh, n_diemsan, n_diemtrungtuyen FROM xt_nganh", Object[].class).list();
            Map<Integer, BigDecimal[]> result = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] != null) {
                    result.put(((Number) row[0]).intValue(), new BigDecimal[]{
                        row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1], // Floor
                        row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2]  // Cutoff
                    });
                }
            }
            return result;
        }
    }

    public Map<Integer, Integer> findMajorQuotas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT idnganh, n_chitieu FROM xt_nganh", Object[].class).list();
            Map<Integer, Integer> result = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] != null && row[1] != null) {
                    result.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
                }
            }
            return result;
        }
    }

    public Map<Integer, String> findMajorRootCombinations() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT idnganh, n_tohopgoc FROM xt_nganh", Object[].class).list();
            Map<Integer, String> result = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] != null && row[1] != null) {
                    result.put(((Number) row[0]).intValue(), row[1].toString());
                }
            }
            return result;
        }
    }
}
