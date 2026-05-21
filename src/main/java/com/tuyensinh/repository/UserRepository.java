package com.tuyensinh.repository;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.User;

public class UserRepository {

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User u order by u.id", User.class).list();
        }
    }

    public User findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        }
    }

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User u where u.username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }

    public void save(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        }
    }

    public void update(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        }
    }

    public List<Object[]> findAllForTable() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = """
                SELECT u.id,
                       u.username,
                       CASE
                           WHEN u.thisinh_id IS NULL THEN 'Quản trị viên'
                           ELSE CONCAT(t.ho, ' ', t.ten)
                       END AS full_name,
                       u.role,
                       u.enabled
                FROM users u
                LEFT JOIN xt_thisinh t ON u.thisinh_id = t.idthisinh
                ORDER BY u.id
            """;

            return session.createNativeQuery(sql).list();
        }
    }
}
