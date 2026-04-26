package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class PatientDAO extends GenericDAO<Patient> {

    public PatientDAO() {
        super(Patient.class);
    }

    /**
     * Search patients by name (HQL LIKE query).
     */
    public List<Patient> searchByName(String name) {
        try (Session session = HibernateUtil.getSession()) {
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
        try (Session session = HibernateUtil.getSession()) {
            long totalPrograms = session.createQuery(
                    "SELECT COUNT(p) FROM TherapyProgram p", Long.class).uniqueResult();

            Query<Patient> query = session.createQuery(
                    "SELECT p FROM Patient p JOIN p.programs pr " +
                            "GROUP BY p HAVING COUNT(pr) = :total", Patient.class);
            query.setParameter("total", totalPrograms);
            return query.list();
        }
    }

    /**
     * Get patients with their enrolled programs (JOIN FETCH to avoid N+1).
     */
    public List<Patient> getAllWithPrograms() {
        try (Session session = HibernateUtil.getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.programs", Patient.class
            ).list();
        }
    }

    /**
     * Find patient by phone number.
     */
    public Patient findByPhone(String phone) {
        try (Session session = HibernateUtil.getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE p.phone = :phone", Patient.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        }
    }
}
