package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.GenericDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PaymentDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentDAOImpl extends GenericDAO<Payment> implements PaymentDAO {

    public PaymentDAOImpl() {
        super(Payment.class);
    }

    /**
     * Find payment by session ID.
     */
    public Payment findBySession(Long sessionId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Payment> query = session.createQuery(
                    "SELECT ts.payment FROM TherapySession ts WHERE ts.id = :sessionId AND ts.payment IS NOT NULL", Payment.class);
            query.setParameter("sessionId", sessionId);
            return query.uniqueResult();
        }
    }

    /**
     * Find payments within a date range.
     */
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

    /**
     * Get total revenue for completed payments within a date range.
     */
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

    /**
     * Get all payments with eagerly fetched session details.
     */
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

    /**
     * Find all payments for a specific patient.
     */
    public List<Payment> findByPatient(Long patientId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "FROM Payment p WHERE p.patient.id = :patientId ORDER BY p.paymentDate DESC",
                    Payment.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    /**
     * Find the upfront payment for a patient (used to link sessions created from upfront credit).
     */
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

    /**
     * Find the upfront payment for a patient using an existing session (for BO-managed transactions).
     */
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

    /**
     * Find payments for a specific patient within a date range.
     */
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

    /**
     * Find payments by type.
     */
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

    /**
     * Filtered search with all criteria (patient, date range, type).
     */
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
