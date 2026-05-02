package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentDAO extends GenericDAO<Payment> {

    public PaymentDAO() {
        super(Payment.class);
    }

    /**
     * Find payment by session ID.
     */
    public Payment findBySession(Long sessionId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Payment> query = session.createQuery(
                    "FROM Payment p WHERE p.session.id = :sessionId", Payment.class);
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
                            "LEFT JOIN FETCH p.session s " +
                            "LEFT JOIN FETCH s.patient " +
                            "ORDER BY p.paymentDate DESC",
                    Payment.class).list();
        }
    }
}
