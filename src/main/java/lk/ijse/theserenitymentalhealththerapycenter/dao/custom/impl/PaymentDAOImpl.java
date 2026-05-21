package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudUtil;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PaymentDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    // ==================== CrudDAO: Self-Contained ====================

    @Override
    public void save(Payment entity) {
        CrudUtil.save(entity);
    }

    @Override
    public void update(Payment entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Payment existing = session.get(Payment.class, entity.getId());
                if (existing != null) {
                    existing.setAmount(entity.getAmount());
                    existing.setMethod(entity.getMethod());
                    existing.setStatus(entity.getStatus());
                    existing.setPaymentType(entity.getPaymentType());
                    existing.setPaymentDate(entity.getPaymentDate());
                    existing.setDescription(entity.getDescription());
                    existing.setDiscount(entity.getDiscount());
                    existing.setPatient(entity.getPatient());
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(Payment entity) {
        CrudUtil.delete(entity);
    }

    @Override
    public Payment getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getById(Payment.class, id, session);
        }
    }

    @Override
    public List<Payment> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getAll(Payment.class, session);
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.count(Payment.class, session);
        }
    }

    // ==================== CrudDAO: Session-Aware ====================

    @Override
    public void save(Payment entity, Session session) {
        CrudUtil.save(entity, session);
    }

    @Override
    public void update(Payment entity, Session session) {
        Payment existing = session.get(Payment.class, entity.getId());
        if (existing != null) {
            existing.setAmount(entity.getAmount());
            existing.setMethod(entity.getMethod());
            existing.setStatus(entity.getStatus());
            existing.setPaymentType(entity.getPaymentType());
            existing.setPaymentDate(entity.getPaymentDate());
            existing.setDescription(entity.getDescription());
            existing.setDiscount(entity.getDiscount());
            existing.setPatient(entity.getPatient());
        }
    }

    @Override
    public void delete(Payment entity, Session session) {
        CrudUtil.delete(entity, session);
    }

    @Override
    public Payment getById(Object id, Session session) {
        return CrudUtil.getById(Payment.class, id, session);
    }

    @Override
    public List<Payment> getAll(Session session) {
        return CrudUtil.getAll(Payment.class, session);
    }

    @Override
    public long count(Session session) {
        return CrudUtil.count(Payment.class, session);
    }

    // ==================== Custom Methods ====================

    @Override
    public Payment findBySession(Long sessionId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Payment> query = session.createQuery(
                    "SELECT ts.payment FROM TherapySession ts WHERE ts.id = :sessionId AND ts.payment IS NOT NULL", Payment.class);
            query.setParameter("sessionId", sessionId);
            return query.uniqueResult();
        }
    }

    @Override
    public List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Payment> query = session.createQuery(
                    "FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC",
                    Payment.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        }
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            BigDecimal result = session.createQuery(
                    "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
                            "WHERE p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate",
                    BigDecimal.class)
                    .setParameter("status", Payment.PaymentStatus.COMPLETED)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        }
    }

    @Override
    public List<Payment> getAllWithDetails() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Payment p " +
                            "LEFT JOIN FETCH p.patient " +
                            "LEFT JOIN FETCH p.coveredSessions " +
                            "ORDER BY p.paymentDate DESC",
                    Payment.class).list();
        }
    }

    @Override
    public List<Payment> findByPatient(Long patientId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "FROM Payment p WHERE p.patient.id = :patientId ORDER BY p.paymentDate DESC",
                    Payment.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    @Override
    public Payment findUpfrontByPatient(Long patientId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "FROM Payment p WHERE p.patient.id = :patientId " +
                            "AND p.paymentType = :paymentType AND p.status = :status " +
                            "ORDER BY p.paymentDate DESC",
                    Payment.class)
                    .setParameter("patientId", patientId)
                    .setParameter("paymentType", Payment.PaymentType.UPFRONT)
                    .setParameter("status", Payment.PaymentStatus.COMPLETED)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    @Override
    public Payment findUpfrontByPatient(Long patientId, Session session) {
        return session.createQuery(
                "FROM Payment p WHERE p.patient.id = :patientId " +
                        "AND p.paymentType = :paymentType AND p.status = :status " +
                        "ORDER BY p.paymentDate DESC",
                Payment.class)
                .setParameter("patientId", patientId)
                .setParameter("paymentType", Payment.PaymentType.UPFRONT)
                .setParameter("status", Payment.PaymentStatus.COMPLETED)
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public List<Payment> findByPatientAndDateRange(Long patientId, LocalDateTime start, LocalDateTime end) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            String hql = "SELECT DISTINCT p FROM Payment p LEFT JOIN FETCH p.patient LEFT JOIN FETCH p.coveredSessions WHERE 1=1";
            if (patientId != null) hql += " AND p.patient.id = :patientId";
            if (start != null && end != null) hql += " AND p.paymentDate BETWEEN :start AND :end";
            hql += " ORDER BY p.paymentDate DESC";

            var query = session.createQuery(hql, Payment.class);
            if (patientId != null) query.setParameter("patientId", patientId);
            if (start != null && end != null) {
                query.setParameter("start", start);
                query.setParameter("end", end);
            }
            return query.list();
        }
    }

    @Override
    public List<Payment> findByType(Payment.PaymentType type) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Payment p LEFT JOIN FETCH p.patient LEFT JOIN FETCH p.coveredSessions " +
                            "WHERE p.paymentType = :type ORDER BY p.paymentDate DESC",
                    Payment.class)
                    .setParameter("type", type)
                    .list();
        }
    }

    @Override
    public List<Payment> findFiltered(Long patientId, LocalDateTime start, LocalDateTime end, Payment.PaymentType type) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            String hql = "SELECT DISTINCT p FROM Payment p LEFT JOIN FETCH p.patient LEFT JOIN FETCH p.coveredSessions WHERE 1=1";
            if (patientId != null) hql += " AND p.patient.id = :patientId";
            if (start != null && end != null) hql += " AND p.paymentDate BETWEEN :start AND :end";
            if (type != null) hql += " AND p.paymentType = :type";
            hql += " ORDER BY p.paymentDate DESC";

            var query = session.createQuery(hql, Payment.class);
            if (patientId != null) query.setParameter("patientId", patientId);
            if (start != null && end != null) {
                query.setParameter("start", start);
                query.setParameter("end", end);
            }
            if (type != null) query.setParameter("type", type);
            return query.list();
        }
    }
}
