package com.tuyensinh.repository;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.XtNganh;

public class NganhRepository {

    public XtNganh findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(XtNganh.class, id);
        }
    }

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

    public List<Object[]> findAllWithAspirationCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT n, (SELECT COUNT(a.id) FROM Aspiration a WHERE a.nganhId = n.idnganh) " +
                        "FROM XtNganh n ORDER BY n.manganh";
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public List<Object[]> searchWithAspirationCount(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT n, (SELECT COUNT(a.id) FROM Aspiration a WHERE a.nganhId = n.idnganh) " +
                        "FROM XtNganh n WHERE n.manganh LIKE :kw OR n.tennganh LIKE :kw ORDER BY n.manganh";
            Query<Object[]> q = session.createQuery(hql, Object[].class);
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

    public boolean existsByManganh(String manganh, Integer namTuyenSinh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery(
                "SELECT COUNT(*) FROM XtNganh WHERE manganh = :ma AND namTuyenSinh = :nam", Long.class);
            q.setParameter("ma", manganh);
            q.setParameter("nam", namTuyenSinh);
            return q.uniqueResult() > 0;
        }
    }

    public void saveAll(List<XtNganh> list) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int count = 0;
            for (XtNganh n : list) {
                XtNganh existing = findByManganhInSession(session, n.getManganh(), n.getNamTuyenSinh());
                if (existing == null) {
                    // Nếu là ngành mới và nChitieu null, gán mặc định 0 để tránh lỗi DB
                    if (n.getNChitieu() == null) n.setNChitieu(0);
                    session.persist(n);
                } else {
                    // Chỉ cập nhật các trường không null từ Excel
                    if (n.getTennganh() != null) existing.setTennganh(n.getTennganh());
                    if (n.getNChitieu() != null) existing.setNChitieu(n.getNChitieu());
                    if (n.getNDiemsan() != null) existing.setNDiemsan(n.getNDiemsan());
                    if (n.getNTohopgoc() != null) existing.setNTohopgoc(n.getNTohopgoc());
                    if (n.getNDgnl() != null) existing.setNDgnl(n.getNDgnl());
                    if (n.getNThpt() != null) existing.setNThpt(n.getNThpt());
                    if (n.getNVsat() != null) existing.setNVsat(n.getNVsat());
                    if (n.getSlXtt() != null) existing.setSlXtt(n.getSlXtt());
                    if (n.getSlDgnl() != null) existing.setSlDgnl(n.getSlDgnl());
                    if (n.getSlVsat() != null) existing.setSlVsat(n.getSlVsat());
                    if (n.getSlThpt() != null) existing.setSlThpt(n.getSlThpt());
                    session.merge(existing);
                }
                
                if (++count % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi import ngành: " + e.getMessage());
        }
    }

    public XtNganh findByManganh(String manganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<XtNganh> q = session.createQuery(
                "FROM XtNganh WHERE manganh = :ma", XtNganh.class);
            q.setParameter("ma", manganh);
            q.setMaxResults(1);
            return q.uniqueResult();
        }
    }

    private XtNganh findByManganhInSession(Session session, String manganh, Integer namTuyenSinh) {
        Query<XtNganh> q = session.createQuery(
            "FROM XtNganh WHERE manganh = :ma AND namTuyenSinh = :nam", XtNganh.class);
        q.setParameter("ma", manganh);
        q.setParameter("nam", namTuyenSinh);
        return q.uniqueResult();
    }
}