package com.tuyensinh.repository;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.EnglishCertificate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EnglishCertificateRepository {

    public List<EnglishCertificate> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM EnglishCertificate", EnglishCertificate.class).list();
        }
    }

    public Map<String, EnglishCertificate> findAllAsMap() {
        List<EnglishCertificate> list = findAll();
        Map<String, EnglishCertificate> map = new HashMap<>();
        for (EnglishCertificate ec : list) {
            map.put(ec.getCccd(), ec);
        }
        return map;
    }

    public EnglishCertificate findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM EnglishCertificate WHERE cccd = :cccd", EnglishCertificate.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
        }
    }

    public void save(EnglishCertificate ec) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(ec);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
