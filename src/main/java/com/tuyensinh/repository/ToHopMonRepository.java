package com.tuyensinh.repository;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.XtToHopMon;

public class ToHopMonRepository {

    public List<XtToHopMon> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM XtToHopMon ORDER BY matohop", XtToHopMon.class).list();
        }
    }

    public List<XtToHopMon> search(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM XtToHopMon WHERE matohop LIKE :kw OR tentohop LIKE :kw " +
                         "OR mon1 LIKE :kw OR mon2 LIKE :kw OR mon3 LIKE :kw ORDER BY matohop";
            Query<XtToHopMon> q = session.createQuery(hql, XtToHopMon.class);
            q.setParameter("kw", "%" + keyword + "%");
            return q.list();
        }
    }

    public void save(XtToHopMon tohop) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(tohop);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi thêm tổ hợp: " + e.getMessage());
        }
    }

    public void update(XtToHopMon tohop) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(tohop);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi cập nhật tổ hợp: " + e.getMessage());
        }
    }

    public void delete(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            XtToHopMon t = session.get(XtToHopMon.class, id);
            if (t != null) session.delete(t);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi xóa tổ hợp: " + e.getMessage());
        }
    }

    public boolean existsByMatohop(String matohop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery(
                "SELECT COUNT(*) FROM XtToHopMon WHERE matohop = :ma", Long.class);
            q.setParameter("ma", matohop);
            return q.uniqueResult() > 0;
        }
    }

    public void saveAll(List<XtToHopMon> list) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (XtToHopMon t : list) {
                if (!existsByMatohopInSession(session, t.getMatohop())) {
                    session.save(t);
                } else {
                    Query<XtToHopMon> q = session.createQuery(
                        "FROM XtToHopMon WHERE matohop = :ma", XtToHopMon.class);
                    q.setParameter("ma", t.getMatohop());
                    XtToHopMon existing = q.uniqueResult();
                    if (existing != null) {
                        existing.setMon1(t.getMon1());
                        existing.setMon2(t.getMon2());
                        existing.setMon3(t.getMon3());
                        existing.setTentohop(t.getTentohop());
                        session.update(existing);
                    }
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi import tổ hợp môn: " + e.getMessage());
        }
    }

    private boolean existsByMatohopInSession(Session session, String matohop) {
        Query<Long> q = session.createQuery(
            "SELECT COUNT(*) FROM XtToHopMon WHERE matohop = :ma", Long.class);
        q.setParameter("ma", matohop);
        return q.uniqueResult() > 0;
    }

    public void mergeByMatohop(XtToHopMon incoming) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            XtToHopMon existing = session.createQuery(
                    "FROM XtToHopMon WHERE matohop = :ma", XtToHopMon.class)
                .setParameter("ma", incoming.getMatohop())
                .uniqueResult();

            if (existing == null) {
                session.persist(incoming);
            } else {
                if (incoming.getTentohop() != null) existing.setTentohop(incoming.getTentohop());
                if (incoming.getMon1()     != null) existing.setMon1(incoming.getMon1());
                if (incoming.getMon2()     != null) existing.setMon2(incoming.getMon2());
                if (incoming.getMon3()     != null) existing.setMon3(incoming.getMon3());
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e.getMessage());
        }
    }
}