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
                    SELECT a, c.ho, c.ten
                    FROM Aspiration a
                    LEFT JOIN Candidate c ON a.cccd = cast(c.idthisinh as string)
                    """;
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String term = "%" + searchTerm.trim().toLowerCase() + "%";
                hql += """
                    WHERE lower(a.cccd) LIKE :term
                       OR lower(a.maNganh) LIKE :term
                       OR lower(a.phuongThuc) LIKE :term
                       OR lower(a.toHop) LIKE :term
                       OR lower(a.ketQua) LIKE :term
                       OR lower(c.ho) LIKE :term
                       OR lower(c.ten) LIKE :term
                       OR lower(a.hoTen) LIKE :term
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
                    SELECT a.maNganh, a.cccd, c.ho, c.ten, a.toHop, a.diemThxt, a.diemUtqd, a.diemCong, a.diemXetTuyen, a.phuongThuc, a.thuTu, a.hoTen
                    FROM Aspiration a
                    LEFT JOIN Candidate c ON a.cccd = cast(c.idthisinh as string)
                    WHERE a.ketQua = 'trungtuyen'
                    ORDER BY a.maNganh, a.diemXetTuyen DESC
                    """;
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public List<Object[]> countSuccessfulByMethodAndMajor() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT a.maNganh, a.phuongThuc, COUNT(a.id)
                    FROM Aspiration a
                    WHERE a.ketQua = 'trungtuyen'
                    GROUP BY a.maNganh, a.phuongThuc
                    ORDER BY a.maNganh, a.phuongThuc
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
            for (Aspiration aspiration : aspirations) {
                session.merge(aspiration);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Khong the luu ket qua xet tuyen: " + e.getMessage(), e);
        }
    }

    public Map<String, BigDecimal> findMajorFloors() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT manganh, n_diemsan FROM xt_nganh", Object[].class).list();
            Map<String, BigDecimal> result = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] != null && row[1] != null) {
                    result.put(row[0].toString(), (BigDecimal) row[1]);
                }
            }
            return result;
        }
    }

    public Map<String, Integer> findMajorQuotas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT manganh, n_chitieu FROM xt_nganh", Object[].class).list();
            Map<String, Integer> result = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] != null && row[1] != null) {
                    result.put(row[0].toString(), ((Number) row[1]).intValue());
                }
            }
            return result;
        }
    }

    public Map<String, String> findMajorRootCombinations() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT manganh, n_tohopgoc FROM xt_nganh", Object[].class).list();
            Map<String, String> result = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] != null && row[1] != null) {
                    result.put(row[0].toString(), row[1].toString());
                }
            }
            return result;
        }
    }
}
