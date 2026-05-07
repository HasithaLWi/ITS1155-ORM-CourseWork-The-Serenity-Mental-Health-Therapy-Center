package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.GenericDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class TherapySessionDAOImpl extends GenericDAO<TherapySession> implements TherapySessionDAO {

    public TherapySessionDAOImpl() {
        super(TherapySession.class);
    }

    /**
     * Find all sessions for a given date.
     */
    public List<TherapySession> findByDate(LocalDate date) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<TherapySession> query = session.createQuery(
                    "FROM TherapySession s WHERE s.sessionDate = :date ORDER BY s.sessionTime", TherapySession.class);
            query.setParameter("date", date);
            return query.list();
        }
    }

    /**
     * Find all sessions for a specific patient.
     */
    public List<TherapySession> findByPatient(Long patientId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<TherapySession> query = session.createQuery(
                    "FROM TherapySession s WHERE s.patient.id = :patientId ORDER BY s.sessionDate DESC", TherapySession.class);
            query.setParameter("patientId", patientId);
            return query.list();
        }
    }

    /**
     * Find all sessions for a specific therapist.
     */
    public List<TherapySession> findByTherapist(Long therapistId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<TherapySession> query = session.createQuery(
                    "FROM TherapySession s WHERE s.therapist.id = :therapistId ORDER BY s.sessionDate DESC", TherapySession.class);
            query.setParameter("therapistId", therapistId);
            return query.list();
        }
    }

    /**
     * Find sessions within a date range.
     */
    public List<TherapySession> findByDateRange(LocalDate startDate, LocalDate endDate) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<TherapySession> query = session.createQuery(
                    "FROM TherapySession s WHERE s.sessionDate BETWEEN :startDate AND :endDate ORDER BY s.sessionDate, s.sessionTime",
                    TherapySession.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        }
    }

    /**
     * Count sessions on a specific date.
     */
    public long countByDate(LocalDate date) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT COUNT(s) FROM TherapySession s WHERE s.sessionDate = :date", Long.class)
                    .setParameter("date", date)
                    .uniqueResult();
        }
    }

    /**
     * Get all sessions with eagerly fetched patient, therapist, and program.
     */
    public List<TherapySession> getAllWithDetails() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT s FROM TherapySession s " +
                            "LEFT JOIN FETCH s.patient " +
                            "LEFT JOIN FETCH s.therapist " +
                            "LEFT JOIN FETCH s.program " +
                            "ORDER BY s.sessionDate DESC, s.sessionTime",
                    TherapySession.class).list();
        }
    }

    /**
     * Find all sessions for a patient enrolled in a specific program, ordered by sequence.
     */
    public List<TherapySession> findByPatientAndProgram(Long patientId, Long programId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "FROM TherapySession s WHERE s.patient.id = :patientId AND s.program.id = :programId " +
                            "ORDER BY s.sequenceNumber",
                    TherapySession.class)
                    .setParameter("patientId", patientId)
                    .setParameter("programId", programId)
                    .list();
        }
    }

    /**
     * Find unscheduled sessions for a patient (across all programs).
     */
    public List<TherapySession> findUnscheduledByPatient(Long patientId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "FROM TherapySession s WHERE s.patient.id = :patientId AND s.status = :status " +
                            "ORDER BY s.program.id, s.sequenceNumber",
                    TherapySession.class)
                    .setParameter("patientId", patientId)
                    .setParameter("status", TherapySession.SessionStatus.UNSCHEDULED)
                    .list();
        }
    }

    /**
     * Count completed sessions for a patient in a specific program (for progress dashboard).
     */
    public long countCompletedByPatientAndProgram(Long patientId, Long programId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT COUNT(s) FROM TherapySession s " +
                            "WHERE s.patient.id = :patientId AND s.program.id = :programId AND s.status = :status",
                    Long.class)
                    .setParameter("patientId", patientId)
                    .setParameter("programId", programId)
                    .setParameter("status", TherapySession.SessionStatus.COMPLETED)
                    .uniqueResult();
        }
    }

    /**
     * Count total sessions for a patient in a specific program.
     */
    public long countByPatientAndProgram(Long patientId, Long programId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT COUNT(s) FROM TherapySession s " +
                            "WHERE s.patient.id = :patientId AND s.program.id = :programId",
                    Long.class)
                    .setParameter("patientId", patientId)
                    .setParameter("programId", programId)
                    .uniqueResult();
        }
    }

    /**
     * Find scheduled sessions for a therapist on a specific date.
     */
    public List<TherapySession> findByTherapistAndDate(Long therapistId, LocalDate date) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "FROM TherapySession s WHERE s.therapist.id = :therapistId AND s.sessionDate = :date " +
                            "AND s.status = :status ORDER BY s.sessionTime",
                    TherapySession.class)
                    .setParameter("therapistId", therapistId)
                    .setParameter("date", date)
                    .setParameter("status", TherapySession.SessionStatus.SCHEDULED)
                    .list();
        }
    }

    /**
     * Bulk update payment status for a list of session IDs.
     */
    public void bulkUpdatePaymentStatus(List<Long> sessionIds, TherapySession.PaymentStatus paymentStatus) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createQuery(
                        "UPDATE TherapySession s SET s.paymentStatus = :status WHERE s.id IN :ids")
                        .setParameter("status", paymentStatus)
                        .setParameter("ids", sessionIds)
                        .executeUpdate();
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        }
    }

    /**
     * Save multiple sessions in a single transaction (for bulk session generation).
     */
    public void saveAll(List<TherapySession> sessions) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                for (TherapySession ts : sessions) {
                    session.persist(ts);
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        }
    }
}
