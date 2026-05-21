package com.tuyensinh.repository;

import com.tuyensinh.entity.MajorCombination;
import com.tuyensinh.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class MajorCombinationRepository {

    // Lấy toàn bộ dữ liệu (Không phân trang)
    public List<MajorCombination> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM MajorCombination", MajorCombination.class).list();
        }
    }

    // Tìm kiếm theo mã ngành hoặc mã tổ hợp (Không phân trang)
    public List<MajorCombination> search(String term) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM MajorCombination WHERE maNganh LIKE :term OR maToHop LIKE :term";
            Query<MajorCombination> query = session.createQuery(hql, MajorCombination.class);
            query.setParameter("term", "%" + term + "%");
            return query.list();
        }
    }

    public void saveOrUpdate(MajorCombination mc) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(mc); // Dùng merge thay cho saveOrUpdate với chuẩn Jakarta mới
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            MajorCombination mc = session.get(MajorCombination.class, id);
            if (mc != null) session.remove(mc); // Dùng remove thay cho delete với chuẩn Jakarta
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void importBatch(List<MajorCombination> list) {
        org.hibernate.Transaction tx = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (MajorCombination mc : list) {
                session.merge(mc);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi import batch Tổ hợp: " + e.getMessage());
        }
    }
}