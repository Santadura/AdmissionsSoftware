package com.tuyensinh.repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.BonusScore;

public class BonusScoreRepository {

    public List<Object[]> findAllWithMajor(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT b, n.manganh
                    FROM BonusScore b
                    LEFT JOIN XtNganh n ON b.nganhId = n.idnganh
                    """;

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String term = "%" + searchTerm.trim().toLowerCase() + "%";
                hql += """
                    WHERE lower(b.cccd) LIKE :term
                       OR lower(n.manganh) LIKE :term
                       OR lower(b.maToHop) LIKE :term
                       OR lower(b.phuongThuc) LIKE :term
                       OR lower(b.dcKeys) LIKE :term
                    """;
                hql += " ORDER BY b.id";
                return session.createQuery(hql, Object[].class)
                        .setParameter("term", term)
                        .list();
            }
            hql += " ORDER BY b.id";
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public BonusScore findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(BonusScore.class, id);
        }
    }

    public Map<String, BigDecimal[]> findBonusTotals() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = """
                    SELECT b.thisinh_cccd,
                           b.nganh_id,
                           b.matohop,
                           COALESCE(NULLIF(TRIM(b.phuongthuc), ''), '') AS phuongthuc_key,
                           SUM(COALESCE(b.diemCC, 0)),
                           SUM(COALESCE(b.diemUtxt, 0))
                    FROM xt_diemcongxetuyen b
                    GROUP BY b.thisinh_cccd,
                             b.nganh_id,
                             b.matohop,
                             COALESCE(NULLIF(TRIM(b.phuongthuc), ''), '')
                    """;
            List<Object[]> rows = session.createNativeQuery(sql, Object[].class).list();
            Map<String, BigDecimal[]> result = new HashMap<>();
            for (Object[] row : rows) {
                result.put(
                        buildBonusKey(row[0], row[1], row[2], row[3]),
                        new BigDecimal[]{
                            row[4] == null ? BigDecimal.ZERO : (BigDecimal) row[4],
                            row[5] == null ? BigDecimal.ZERO : (BigDecimal) row[5]
                        });
            }
            return result;
        }
    }

    public void save(BonusScore bonusScore) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(bonusScore);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Khong the luu diem cong: " + e.getMessage(), e);
        }
    }

    public void update(BonusScore bonusScore) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(bonusScore);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Khong the cap nhat diem cong: " + e.getMessage(), e);
        }
    }

    public void delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BonusScore bonusScore = session.get(BonusScore.class, id);
            if (bonusScore != null) {
                session.remove(bonusScore);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Khong the xoa diem cong: " + e.getMessage(), e);
        }
    }

    public void saveAll(List<BonusScore> list) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int count = 0;
            for (BonusScore b : list) {
                BonusScore existing = findByDcKeys(session, b.getDcKeys());
                if (existing == null) {
                    session.persist(b);
                } else {
                    // Merging logic: Only update fields that are present (non-zero) in the import
                    boolean changed = false;
                    if (b.getDiemCc() != null && b.getDiemCc().compareTo(BigDecimal.ZERO) > 0) {
                        existing.setDiemCc(b.getDiemCc());
                        changed = true;
                    }
                    if (b.getDiemUtxt() != null && b.getDiemUtxt().compareTo(BigDecimal.ZERO) > 0) {
                        existing.setDiemUtxt(b.getDiemUtxt());
                        changed = true;
                    }

                    if (changed) {
                        // Recalculate total
                        BigDecimal cc = existing.getDiemCc() == null ? BigDecimal.ZERO : existing.getDiemCc();
                        BigDecimal ut = existing.getDiemUtxt() == null ? BigDecimal.ZERO : existing.getDiemUtxt();
                        existing.setDiemTong(cc.add(ut));

                        // Merge ghiChu
                        if (b.getGhiChu() != null && !b.getGhiChu().trim().isEmpty()) {
                            String oldGhiChu = existing.getGhiChu();
                            if (oldGhiChu == null || oldGhiChu.trim().isEmpty()) {
                                existing.setGhiChu(b.getGhiChu());
                            } else if (!oldGhiChu.contains(b.getGhiChu())) {
                                existing.setGhiChu(oldGhiChu + "; " + b.getGhiChu());
                            }
                        }
                        session.merge(existing);
                    }
                }
                if (++count % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Lỗi import điểm cộng: " + e.getMessage());
        }
    }

    private BonusScore findByDcKeys(Session session, String dcKeys) {
        if (dcKeys == null) return null;
        Query<BonusScore> q = session.createQuery("FROM BonusScore WHERE dcKeys = :key", BonusScore.class);
        q.setParameter("key", dcKeys);
        return q.uniqueResult();
    }

    public BigDecimal sumBonus(String cccd, Integer nganhId, String maToHop, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("""
                    SELECT SUM(b.diemTong)
                    FROM BonusScore b
                    WHERE b.cccd = :cccd
                      AND b.nganhId = :nganhId
                    """);

            if (hasText(maToHop)) {
                hql.append(" AND b.maToHop = :maToHop");
            } else {
                hql.append(" AND (b.maToHop IS NULL OR b.maToHop = '')");
            }
            if (hasText(phuongThuc)) {
                hql.append(" AND (b.phuongThuc = :phuongThuc OR b.phuongThuc IS NULL OR b.phuongThuc = '')");
            } else {
                hql.append(" AND (b.phuongThuc IS NULL OR b.phuongThuc = '')");
            }

            var query = session.createQuery(hql.toString(), BigDecimal.class)
                    .setParameter("cccd", cccd)
                    .setParameter("nganhId", nganhId);
            if (hasText(maToHop)) {
                query.setParameter("maToHop", maToHop);
            }
            if (hasText(phuongThuc)) {
                query.setParameter("phuongThuc", phuongThuc);
            }

            BigDecimal result = query.uniqueResult();
            return result == null ? BigDecimal.ZERO : result;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String buildBonusKey(Object cccd, Object nganhId, Object maToHop, Object phuongThuc) {
        return keyPart(cccd) + "|" + keyPart(nganhId) + "|" + keyPart(maToHop) + "|" + keyPart(phuongThuc);
    }

    private static String keyPart(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
