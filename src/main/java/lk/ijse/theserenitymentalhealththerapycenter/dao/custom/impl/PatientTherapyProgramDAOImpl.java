package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientTherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PatientTherapyProgramDAOImpl implements PatientTherapyProgramDAO {



    @Override
    public void save(PatientTherapyProgram entity) {
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
    public void update(PatientTherapyProgram entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                PatientTherapyProgram existing = session.get(PatientTherapyProgram.class, entity.getId());
                if (existing != null) {
                    existing.setPatient(entity.getPatient());
                    existing.setProgram(entity.getProgram());
                    existing.setSessionsPaid(entity.getSessionsPaid());
                    existing.setSessionsUsed(entity.getSessionsUsed());
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(PatientTherapyProgram entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                PatientTherapyProgram merged = session.merge(entity);
                session.remove(merged);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public PatientTherapyProgram getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(PatientTherapyProgram.class, id);
        }
    }

    @Override
    public List<PatientTherapyProgram> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM PatientTherapyProgram", PatientTherapyProgram.class).list();
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("SELECT COUNT(e) FROM PatientTherapyProgram e", Long.class)
                    .uniqueResult();
        }
    }



    @Override
    public void save(PatientTherapyProgram entity, Session session) {
        session.persist(entity);
    }

    @Override
    public void update(PatientTherapyProgram entity, Session session) {
        PatientTherapyProgram existing = session.get(PatientTherapyProgram.class, entity.getId());
        if (existing != null) {
            existing.setPatient(entity.getPatient());
            existing.setProgram(entity.getProgram());
            existing.setSessionsPaid(entity.getSessionsPaid());
            existing.setSessionsUsed(entity.getSessionsUsed());
        }
    }

    @Override
    public void delete(PatientTherapyProgram entity, Session session) {
        PatientTherapyProgram merged = session.merge(entity);
        session.remove(merged);
    }

    @Override
    public PatientTherapyProgram getById(Object id, Session session) {
        return session.get(PatientTherapyProgram.class, id);
    }

    @Override
    public List<PatientTherapyProgram> getAll(Session session) {
        return session.createQuery("FROM PatientTherapyProgram", PatientTherapyProgram.class).list();
    }

    @Override
    public long count(Session session) {
        return session.createQuery("SELECT COUNT(e) FROM PatientTherapyProgram e", Long.class)
                .uniqueResult();
    }



    @Override
    public List<PatientTherapyProgram> findByPatient(Long patientId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT ptp FROM PatientTherapyProgram ptp " +
                            "LEFT JOIN FETCH ptp.patient " +
                            "LEFT JOIN FETCH ptp.program " +
                            "WHERE ptp.patient.id = :patientId",
                    PatientTherapyProgram.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    @Override
    public PatientTherapyProgram findByPatientAndProgram(Long patientId, Long programId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return findByPatientAndProgram(patientId, programId, session);
        }
    }

    @Override
    public PatientTherapyProgram findByPatientAndProgram(Long patientId, Long programId, Session session) {
        return session.createQuery(
                "SELECT DISTINCT ptp FROM PatientTherapyProgram ptp " +
                        "LEFT JOIN FETCH ptp.patient " +
                        "LEFT JOIN FETCH ptp.program " +
                        "WHERE ptp.patient.id = :patientId AND ptp.program.id = :programId",
                PatientTherapyProgram.class)
                .setParameter("patientId", patientId)
                .setParameter("programId", programId)
                .uniqueResult();
    }

    @Override
    public void deductCredit(Long patientId, Long programId, Session session) {
        session.createQuery(
                "UPDATE PatientTherapyProgram ptp " +
                        "SET ptp.sessionsUsed = ptp.sessionsUsed + 1 " +
                        "WHERE ptp.patient.id = :patientId AND ptp.program.id = :programId")
                .setParameter("patientId", patientId)
                .setParameter("programId", programId)
                .executeUpdate();
    }

    @Override
    public void saveAll(List<PatientTherapyProgram> enrollments, Session session) {
        for (PatientTherapyProgram ptp : enrollments) {
            session.persist(ptp);
        }
    }
}
