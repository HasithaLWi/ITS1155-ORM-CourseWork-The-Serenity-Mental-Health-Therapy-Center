package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.GenericDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import org.hibernate.Session;
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
}
