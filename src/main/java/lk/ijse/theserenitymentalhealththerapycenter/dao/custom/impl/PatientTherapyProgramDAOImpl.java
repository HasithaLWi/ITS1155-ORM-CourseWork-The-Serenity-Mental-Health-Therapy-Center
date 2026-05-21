package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudUtil;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientTherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PatientTherapyProgramDAOImpl implements PatientTherapyProgramDAO {

    // ==================== CrudDAO: Self-Contained ====================

    @Override
    public void save(PatientTherapyProgram entity) {
        CrudUtil.save(entity);
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
                    existing.setUpfrontSessionsPaid(entity.getUpfrontSessionsPaid());
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
        CrudUtil.delete(entity);
    }

    @Override
    public PatientTherapyProgram getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getById(PatientTherapyProgram.class, id, session);
        }
    }

    @Override
    public List<PatientTherapyProgram> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getAll(PatientTherapyProgram.class, session);
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.count(PatientTherapyProgram.class, session);
        }
    }

    // ==================== CrudDAO: Session-Aware ====================

    @Override
    public void save(PatientTherapyProgram entity, Session session) {
        CrudUtil.save(entity, session);
    }

    @Override
    public void update(PatientTherapyProgram entity, Session session) {
        PatientTherapyProgram existing = session.get(PatientTherapyProgram.class, entity.getId());
        if (existing != null) {
            existing.setPatient(entity.getPatient());
            existing.setProgram(entity.getProgram());
            existing.setUpfrontSessionsPaid(entity.getUpfrontSessionsPaid());
            existing.setSessionsUsed(entity.getSessionsUsed());
        }
    }

    @Override
    public void delete(PatientTherapyProgram entity, Session session) {
        CrudUtil.delete(entity, session);
    }

    @Override
    public PatientTherapyProgram getById(Object id, Session session) {
        return CrudUtil.getById(PatientTherapyProgram.class, id, session);
    }

    @Override
    public List<PatientTherapyProgram> getAll(Session session) {
        return CrudUtil.getAll(PatientTherapyProgram.class, session);
    }

    @Override
    public long count(Session session) {
        return CrudUtil.count(PatientTherapyProgram.class, session);
    }

    // ==================== Custom Methods ====================

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
            CrudUtil.save(ptp, session);
        }
    }
}
