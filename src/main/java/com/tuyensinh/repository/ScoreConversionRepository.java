package com.tuyensinh.repository;

import com.tuyensinh.entity.ScoreConversion;
import com.tuyensinh.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class ScoreConversionRepository {

    // Lấy toàn bộ dữ liệu 
    public List<ScoreConversion> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ScoreConversion", ScoreConversion.class).list();
        }
    }

    // Tìm kiếm trên toàn bộ dữ liệu 
    public List<ScoreConversion> search(String term) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ScoreConversion WHERE phuongThuc LIKE :term OR toHop LIKE :term OR maQuyDoi LIKE :term";
            Query<ScoreConversion> query = session.createQuery(hql, ScoreConversion.class);
            query.setParameter("term", "%" + term + "%");
            return query.list();
        }
    }

    public void saveOrUpdate(ScoreConversion sc) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(sc);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void saveAll(List<ScoreConversion> list) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int count = 0;
            for (ScoreConversion sc : list) {
                session.merge(sc);
                if (++count % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi import bảng quy đổi: " + e.getMessage());
        }
    }

    public void delete(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ScoreConversion sc = session.get(ScoreConversion.class, id);
            if (sc != null) session.remove(sc);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}