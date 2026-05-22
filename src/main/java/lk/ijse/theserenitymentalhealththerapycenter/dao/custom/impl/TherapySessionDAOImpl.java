package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class TherapySessionDAOImpl implements TherapySessionDAO {



    @Override
    public void save(TherapySession entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(entity);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void update(TherapySession entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                TherapySession existing = session.get(TherapySession.class, entity.getId());
                if (existing != null) {
                    existing.setPatient(entity.getPatient());
                    existing.setTherapist(entity.getTherapist());
                    existing.setProgram(entity.getProgram());
                    existing.setSessionDate(entity.getSessionDate());
                    existing.setSessionTime(entity.getSessionTime());
                    existing.setStatus(entity.getStatus());
                    existing.setPaymentStatus(entity.getPaymentStatus());
                    existing.setNotes(entity.getNotes());
                    existing.setSequenceNumber(entity.getSequenceNumber());
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(TherapySession entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                TherapySession merged = session.merge(entity);
                session.remove(merged);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public TherapySession getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT s FROM TherapySession s " +
                            "LEFT JOIN FETCH s.patient " +
                            "LEFT JOIN FETCH s.therapist " +
                            "LEFT JOIN FETCH s.program " +
                            "WHERE s.id = :id",
                    TherapySession.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    @Override
    public List<TherapySession> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM TherapySession", TherapySession.class).list();
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("SELECT COUNT(e) FROM TherapySession e", Long.class)
                    .uniqueResult();
        }
    }

    @Override
    public void save(TherapySession entity, Session session) {
        session.persist(entity);
    }

    @Override
    public void update(TherapySession entity, Session session) {
        TherapySession existing = session.get(TherapySession.class, entity.getId());
        if (existing != null) {
            existing.setPatient(entity.getPatient());
            existing.setTherapist(entity.getTherapist());
            existing.setProgram(entity.getProgram());
            existing.setSessionDate(entity.getSessionDate());
            existing.setSessionTime(entity.getSessionTime());
            existing.setStatus(entity.getStatus());
            existing.setPaymentStatus(entity.getPaymentStatus());
            existing.setNotes(entity.getNotes());
            existing.setSequenceNumber(entity.getSequenceNumber());
        }
    }

    @Override
    public void delete(TherapySession entity, Session session) {
        TherapySession merged = session.merge(entity);
        session.remove(merged);
    }

    @Override
    public TherapySession getById(Object id, Session session) {
        return session.createQuery(
                "SELECT DISTINCT s FROM TherapySession s " +
                        "LEFT JOIN FETCH s.patient " +
                        "LEFT JOIN FETCH s.therapist " +
                        "LEFT JOIN FETCH s.program " +
                        "WHERE s.id = :id",
                TherapySession.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public List<TherapySession> getAll(Session session) {
        return session.createQuery("FROM TherapySession", TherapySession.class).list();
    }

    @Override
    public long count(Session session) {
        return session.createQuery("SELECT COUNT(e) FROM TherapySession e", Long.class)
                .uniqueResult();
    }

    @Override
    public List<TherapySession> findByDate(LocalDate date) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT s FROM TherapySession s " +
                            "LEFT JOIN FETCH s.patient " +
                            "LEFT JOIN FETCH s.therapist " +
                            "LEFT JOIN FETCH s.program " +
                            "WHERE s.sessionDate = :date ORDER BY s.sessionTime", TherapySession.class)
                    .setParameter("date", date)
                    .list();
        }
    }

    @Override
    public List<TherapySession> findByPatient(Long patientId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT s FROM TherapySession s " +
                            "LEFT JOIN FETCH s.patient " +
                            "LEFT JOIN FETCH s.therapist " +
                            "LEFT JOIN FETCH s.program " +
                            "WHERE s.patient.id = :patientId ORDER BY s.sessionDate DESC", TherapySession.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    @Override
    public List<TherapySession> findByTherapist(Long therapistId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT s FROM TherapySession s " +
                            "LEFT JOIN FETCH s.patient " +
                            "LEFT JOIN FETCH s.therapist " +
                            "LEFT JOIN FETCH s.program " +
                            "WHERE s.therapist.id = :therapistId ORDER BY s.sessionDate DESC", TherapySession.class)
                    .setParameter("therapistId", therapistId)
                    .list();
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public List<TherapySession> getScheduledSessionsSortedByDate() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT s FROM TherapySession s " +
                            "LEFT JOIN FETCH s.patient " +
                            "LEFT JOIN FETCH s.therapist " +
                            "LEFT JOIN FETCH s.program " +
                            "WHERE s.status = :status " +
                            "ORDER BY s.sessionDate ASC, s.sessionTime ASC",
                    TherapySession.class)
                    .setParameter("status", TherapySession.SessionStatus.SCHEDULED)
                    .list();
        }
    }
}
