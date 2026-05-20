package com.tuyensinh.repository;

import java.math.BigDecimal;
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
            Query<XtNganh> q = session.createQuery(
                "FROM XtNganh WHERE manganh LIKE :kw OR tennganh LIKE :kw ORDER BY manganh",
                XtNganh.class);
            q.setParameter("kw", "%" + keyword + "%");
            return q.list();
        }
    }

    // Sửa save() - dùng persist() thay save()
    public void save(XtNganh nganh) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(nganh); // ← thay session.save()
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi thêm ngành: " + e.getMessage());
        }
    }

    // Sửa update() - dùng merge() đúng cách, nhận về entity mới
    public void update(XtNganh nganh) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(nganh);
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
                    XtNganh existing = session.createQuery(
                        "FROM XtNganh WHERE manganh = :ma", XtNganh.class)
                        .setParameter("ma", n.getManganh())
                        .uniqueResult();
                    if (existing != null) {
                        existing.setTennganh(n.getTennganh());
                        existing.setNChitieu(n.getNChitieu());
                        existing.setNDiemsan(n.getNDiemsan());
                        existing.setNTohopgoc(n.getNTohopgoc());
                        existing.setNDgnl(n.getNDgnl());
                        existing.setNThpt(n.getNThpt());
                        existing.setNVsat(n.getNVsat());
                        // ← KHÔNG cần merge/update: entity đang managed trong session này
                        // Hibernate tự dirty-check khi commit
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

    public void updateDiemChuan(String maNganh, BigDecimal diemChuan) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            XtNganh nganh = session.createQuery(
                "FROM XtNganh WHERE manganh = :ma", XtNganh.class)
                .setParameter("ma", maNganh)
                .uniqueResult();
            if (nganh != null) {
                nganh.setNDiemtrungtuyen(diemChuan);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi cập nhật điểm chuẩn: " + e.getMessage());
        }
    }

    public XtNganh findByManganh(String manganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM XtNganh WHERE manganh = :manganh", XtNganh.class)
                .setParameter("manganh", manganh)
                .uniqueResult();
        }
    }

    public void importBatch(List<XtNganh> list) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            for (XtNganh incoming : list) {
                XtNganh existing = session.createQuery(
                        "FROM XtNganh WHERE manganh = :ma", XtNganh.class)
                    .setParameter("ma", incoming.getManganh())
                    .uniqueResult();

                if (existing == null) {
                    if (incoming.getNChitieu() != null && incoming.getNChitieu() == -1) {
                        incoming.setNChitieu(0);
                    }
                    if ("UNMODIFIED".equals(incoming.getNThpt())) incoming.setNThpt("0");
                    if ("UNMODIFIED".equals(incoming.getNDgnl())) incoming.setNDgnl("0");
                    if ("UNMODIFIED".equals(incoming.getNVsat())) incoming.setNVsat("0");
                    
                    session.persist(incoming);
                } else {
                    if (incoming.getTennganh() != null) existing.setTennganh(incoming.getTennganh());
                    if (incoming.getNTohopgoc() != null) existing.setNTohopgoc(incoming.getNTohopgoc());
                    if (incoming.getNDiemsan() != null) existing.setNDiemsan(incoming.getNDiemsan());
                    
                    if (incoming.getNChitieu() != null && incoming.getNChitieu() != -1) {
                        existing.setNChitieu(incoming.getNChitieu());
                    }
                    if (incoming.getNThpt() != null && !"UNMODIFIED".equals(incoming.getNThpt())) {
                        existing.setNThpt(incoming.getNThpt());
                    }
                    if (incoming.getNDgnl() != null && !"UNMODIFIED".equals(incoming.getNDgnl())) {
                        existing.setNDgnl(incoming.getNDgnl());
                    }
                    if (incoming.getNVsat() != null && !"UNMODIFIED".equals(incoming.getNVsat())) {
                        existing.setNVsat(incoming.getNVsat());
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi import batch: " + e.getMessage());
        }
    }
}