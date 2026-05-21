package com.tuyensinh.repository;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.Candidate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class CandidateRepository {
    
    public void saveAll(List<Candidate> candidates) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int count = 0;
            for (Candidate candidate : candidates) {
                Candidate existing = findByCccd(session, candidate.getCccd());
                if (existing == null) {
                    session.persist(candidate);
                } else {
                    existing.setHo(candidate.getHo());
                    existing.setTen(candidate.getTen());
                    existing.setNgaySinh(candidate.getNgaySinh());
                    existing.setGioiTinh(candidate.getGioiTinh());
                    existing.setNoiSinh(candidate.getNoiSinh());
                    existing.setDoiTuong(candidate.getDoiTuong());
                    existing.setKhuVuc(candidate.getKhuVuc());
                    existing.setDienThoai(candidate.getDienThoai());
                    existing.setEmail(candidate.getEmail());
                    existing.setNamTuyenSinh(candidate.getNamTuyenSinh());
                    existing.setUpdatedAt(java.time.LocalDate.now());
                    session.merge(existing);
                }
                
                if (++count % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi khi lưu danh sách thí sinh: " + e.getMessage());
        }
    }
    
    public List<Candidate> findAll(int page, int pageSize) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Candidate> query = session.createQuery(
                "FROM Candidate ORDER BY idthisinh", Candidate.class);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }
    
    public List<Candidate> search(String searchTerm, int page, int pageSize) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Candidate WHERE " +
                        "cccd LIKE :term OR " +
                        "ho LIKE :term OR " +
                        "ten LIKE :term OR " +
                        "CONCAT(ho, ' ', ten) LIKE :term " +
                        "ORDER BY idthisinh";
            Query<Candidate> query = session.createQuery(hql, Candidate.class);
            query.setParameter("term", "%" + searchTerm + "%");
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }
    
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM Candidate", Long.class);
            return query.uniqueResult();
        }
    }
    
    public long countBySearch(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(*) FROM Candidate WHERE " +
                        "cccd LIKE :term OR " +
                        "ho LIKE :term OR " +
                        "ten LIKE :term OR " +
                        "CONCAT(ho, ' ', ten) LIKE :term";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("term", "%" + searchTerm + "%");
            return query.uniqueResult();
        }
    }

    public List<Object[]> getStatisticsByObject() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT doiTuong, COUNT(*) FROM Candidate GROUP BY doiTuong";
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public List<Object[]> getStatisticsByRegion() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT khuVuc, COUNT(*) FROM Candidate GROUP BY khuVuc";
            return session.createQuery(hql, Object[].class).list();
        }
    }
    
    public void save(Candidate candidate) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(candidate);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi lưu thí sinh: " + e.getMessage());
        }
    }

    public void update(Candidate candidate) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(candidate);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi cập nhật thí sinh: " + e.getMessage());
        }
    }

    public void delete(Candidate candidate) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(candidate) ? candidate : session.merge(candidate));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi xóa thí sinh: " + e.getMessage());
        }
    }
    
    public Candidate findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Candidate.class, id);
        }
    }
    
    public Candidate findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return findByCccd(session, cccd);
        }
    }

    private Candidate findByCccd(Session session, String cccd) {
        Query<Candidate> query = session.createQuery(
            "FROM Candidate WHERE cccd = :cccd", Candidate.class);
        query.setParameter("cccd", cccd);
        return query.uniqueResult();
    }
}
