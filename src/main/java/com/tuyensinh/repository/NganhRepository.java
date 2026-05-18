package com.tuyensinh.repository;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.XtNganh;

public class NganhRepository {

    public List<XtNganh> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM XtNganh ORDER BY manganh", XtNganh.class).list();
        }
    }

    public List<XtNganh> search(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM XtNganh WHERE manganh LIKE :kw OR tennganh LIKE :kw ORDER BY manganh";
            Query<XtNganh> q = session.createQuery(hql, XtNganh.class);
            q.setParameter("kw", "%" + keyword + "%");
            return q.list();
        }
    }

    public void save(XtNganh nganh) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(nganh);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi thêm ngành: " + e.getMessage());
        }
    }

    public void update(XtNganh nganh) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(nganh);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi cập nhật ngành: " + e.getMessage());
        }
    }

    public void delete(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            XtNganh nganh = session.get(XtNganh.class, id);
            if (nganh != null) session.delete(nganh);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi xóa ngành: " + e.getMessage());
        }
    }

    public boolean existsByManganh(String manganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery(
                "SELECT COUNT(*) FROM XtNganh WHERE manganh = :ma", Long.class);
            q.setParameter("ma", manganh);
            return q.uniqueResult() > 0;
        }
    }

    public void saveAll(List<XtNganh> list) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (XtNganh n : list) {
                if (!existsByManganhInSession(session, n.getManganh())) {
                    session.save(n);
                } else {
                    Query<XtNganh> q = session.createQuery(
                        "FROM XtNganh WHERE manganh = :ma", XtNganh.class);
                    q.setParameter("ma", n.getManganh());
                    XtNganh existing = q.uniqueResult();
                    if (existing != null) {
                        existing.setTennganh(n.getTennganh());
                        existing.setNChitieu(n.getNChitieu());
                        existing.setNDiemsan(n.getNDiemsan());
                        existing.setNTohopgoc(n.getNTohopgoc());
                        existing.setNDgnl(n.getNDgnl());
                        existing.setNThpt(n.getNThpt());
                        existing.setNVsat(n.getNVsat());
                        session.update(existing);
                    }
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi import ngành: " + e.getMessage());
        }
    }

    private boolean existsByManganhInSession(Session session, String manganh) {
        Query<Long> q = session.createQuery(
            "SELECT COUNT(*) FROM XtNganh WHERE manganh = :ma", Long.class);
        q.setParameter("ma", manganh);
        return q.uniqueResult() > 0;
    }
}