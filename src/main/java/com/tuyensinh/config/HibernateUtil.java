package com.tuyensinh.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.tuyensinh.entity.User;
import com.tuyensinh.entity.Candidate;
import com.tuyensinh.entity.CandidateScore;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {

        try {

            return new Configuration()
                    .configure("hibernate.cfg.xml")

                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Candidate.class)
                    .addAnnotatedClass(CandidateScore.class)

                    .buildSessionFactory();

        } catch (Throwable ex) {

            System.err.println(
                    "Lỗi khởi tạo SessionFactory: "
                            + ex.getMessage()
            );

            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}