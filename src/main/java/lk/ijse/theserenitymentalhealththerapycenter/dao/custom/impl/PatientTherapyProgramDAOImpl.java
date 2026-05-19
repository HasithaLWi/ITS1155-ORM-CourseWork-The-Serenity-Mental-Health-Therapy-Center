package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.GenericDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientTherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class PatientTherapyProgramDAOImpl extends GenericDAO<PatientTherapyProgram> implements PatientTherapyProgramDAO {

    public PatientTherapyProgramDAOImpl() {
        super(PatientTherapyProgram.class);
    }

    /**
     * Find all program enrollments for a patient with their credit info.
     */
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

    /**
     * Find a specific patient-program enrollment.
     */
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

    /**
     * Increment sessionsUsed for a patient-program enrollment (deduct one credit).
     */
    public void deductCredit(Long patientId, Long programId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.createQuery(
                        "UPDATE PatientTherapyProgram ptp " +
                                "SET ptp.sessionsUsed = ptp.sessionsUsed + 1 " +
                                "WHERE ptp.patient.id = :patientId AND ptp.program.id = :programId")
                        .setParameter("patientId", patientId)
                        .setParameter("programId", programId)
                        .executeUpdate();
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    /**
     * Save multiple enrollments in a single transaction.
     */
    public void saveAll(List<PatientTherapyProgram> enrollments) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                for (PatientTherapyProgram ptp : enrollments) {
                    session.persist(ptp);
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }
}
