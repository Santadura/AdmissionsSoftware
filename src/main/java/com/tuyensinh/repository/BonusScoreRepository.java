package com.tuyensinh.repository;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.BonusScore;

public class BonusScoreRepository {

    public List<BonusScore> findAll(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return session.createQuery("FROM BonusScore b ORDER BY b.id", BonusScore.class).list();
            }

            String term = "%" + searchTerm.trim().toLowerCase() + "%";
            String hql = """
                    FROM BonusScore b
                    WHERE lower(b.cccd) LIKE :term
                       OR lower(b.maNganh) LIKE :term
                       OR lower(b.maToHop) LIKE :term
                       OR lower(b.phuongThuc) LIKE :term
                       OR lower(b.dcKeys) LIKE :term
                    ORDER BY b.id
                    """;
            return session.createQuery(hql, BonusScore.class)
                    .setParameter("term", term)
                    .list();
        }
    }

    public BonusScore findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(BonusScore.class, id);
        }
    }

    public void save(BonusScore bonusScore) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(bonusScore);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Khong the luu diem cong: " + e.getMessage(), e);
        }
    }

    public void update(BonusScore bonusScore) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(bonusScore);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Khong the cap nhat diem cong: " + e.getMessage(), e);
        }
    }

    public void delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BonusScore bonusScore = session.get(BonusScore.class, id);
            if (bonusScore != null) {
                session.remove(bonusScore);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Khong the xoa diem cong: " + e.getMessage(), e);
        }
    }

    public BigDecimal sumBonus(String cccd, String maNganh, String maToHop, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("""
                    SELECT SUM(b.diemTong)
                    FROM BonusScore b
                    WHERE b.cccd = :cccd
                      AND b.maNganh = :maNganh
                    """);

            if (hasText(maToHop)) {
                hql.append(" AND b.maToHop = :maToHop");
            } else {
                hql.append(" AND (b.maToHop IS NULL OR b.maToHop = '')");
            }
            if (hasText(phuongThuc)) {
                hql.append(" AND b.phuongThuc = :phuongThuc");
            } else {
                hql.append(" AND (b.phuongThuc IS NULL OR b.phuongThuc = '')");
            }

            var query = session.createQuery(hql.toString(), BigDecimal.class)
                    .setParameter("cccd", cccd)
                    .setParameter("maNganh", maNganh);
            if (hasText(maToHop)) {
                query.setParameter("maToHop", maToHop);
            }
            if (hasText(phuongThuc)) {
                query.setParameter("phuongThuc", phuongThuc);
            }

            BigDecimal result = query.uniqueResult();
            return result == null ? BigDecimal.ZERO : result;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
