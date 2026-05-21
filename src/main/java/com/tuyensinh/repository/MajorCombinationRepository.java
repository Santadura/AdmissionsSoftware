package com.tuyensinh.repository;

import com.tuyensinh.entity.MajorCombination;
import com.tuyensinh.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class MajorCombinationRepository {

    public List<Object[]> findAllWithMajor() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT m, n.manganh
                    FROM MajorCombination m
                    LEFT JOIN XtNganh n ON m.nganhId = n.idnganh
                    ORDER BY m.id
                    """;
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public List<Object[]> searchWithMajor(String term) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT m, n.manganh
                    FROM MajorCombination m
                    LEFT JOIN XtNganh n ON m.nganhId = n.idnganh
                    WHERE lower(n.manganh) LIKE :term OR lower(m.maToHop) LIKE :term
                    ORDER BY m.id
                    """;
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("term", "%" + term.toLowerCase() + "%");
            return query.list();
        }
    }

    public List<MajorCombination> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM MajorCombination", MajorCombination.class).list();
        }
    }

    public void saveOrUpdate(MajorCombination mc) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(mc);
            transaction.commit();
                } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi khóa ngoại: Tham chiếu ID Ngành không tồn tại!");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Khong the luu To hop mong: " + e.getMessage(), e);
        }
    }

    public void saveAll(List<MajorCombination> list) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int count = 0;
            for (MajorCombination mc : list) {
                MajorCombination existing = findByTbKeys(session, mc.getTbKeys());
                if (existing == null) {
                    session.persist(mc);
                } else {
                    existing.setThMon1(mc.getThMon1());
                    existing.setHsMon1(mc.getHsMon1());
                    existing.setThMon2(mc.getThMon2());
                    existing.setHsMon2(mc.getHsMon2());
                    existing.setThMon3(mc.getThMon3());
                    existing.setHsMon3(mc.getHsMon3());
                    existing.setDoLech(mc.getDoLech());
                    session.merge(existing);
                }
                if (++count % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
                } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi khóa ngoại: Tham chiếu ID Ngành không tồn tại!");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi import tổ hợp ngành: " + e.getMessage());
        }
    }

    private MajorCombination findByTbKeys(Session session, String tbKeys) {
        if (tbKeys == null) return null;
        Query<MajorCombination> q = session.createQuery("FROM MajorCombination WHERE tbKeys = :key", MajorCombination.class);
        q.setParameter("key", tbKeys);
        return q.uniqueResult();
    }

    public MajorCombination findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MajorCombination.class, id);
        }
    }

    public List<MajorCombination> findByNganhId(Integer nganhId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MajorCombination> query = session.createQuery("FROM MajorCombination WHERE nganhId = :nganhId", MajorCombination.class);
            query.setParameter("nganhId", nganhId);
            return query.list();
        }
    }

    public void delete(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            MajorCombination mc = session.get(MajorCombination.class, id);
            if (mc != null) session.remove(mc);
            transaction.commit();
                } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi khóa ngoại: Tham chiếu ID Ngành không tồn tại!");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Khong the xoa to hop mong: " + e.getMessage(), e);
        }
    }
}
