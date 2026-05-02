package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class PatientDAO extends GenericDAO<Patient> {

    @Override
    public List<Patient> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM Patient ", Patient.class)
                    .setCacheable(true)
                    .list();


        }
    }

    public PatientDAO() {
        super(Patient.class);
    }

    /**
     * Search patients by name (HQL LIKE query).
     */
    public List<Patient> searchByName(String name) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE LOWER(p.name) LIKE LOWER(:name)", Patient.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }

    /**
     * Find patients who are enrolled in ALL therapy programs (HQL with HAVING COUNT).
     */
    public List<Patient> findPatientsInAllPrograms() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            long totalPrograms = session.createQuery(
                    "SELECT COUNT(tp) FROM TherapyProgram tp", Long.class).uniqueResult();

            Query<Patient> query = session.createQuery(
                    "SELECT p FROM Patient p JOIN p.patientTherapyPrograms ptp " +
                            "GROUP BY p HAVING COUNT(ptp) = :total", Patient.class);
            query.setParameter("total", totalPrograms);
            return query.list();
        }
    }

    /**
     * Get patients with their enrolled programs (JOIN FETCH to avoid N+1).
     */
    public List<Patient> getAllWithPrograms() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.patientTherapyPrograms ptp LEFT JOIN FETCH ptp.program", Patient.class
            ).list();
        }
    }

    /**
     * Find patient by phone number.
     */
    public Patient findByPhone(String phone) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE p.phone = :phone", Patient.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        }
    }
}
