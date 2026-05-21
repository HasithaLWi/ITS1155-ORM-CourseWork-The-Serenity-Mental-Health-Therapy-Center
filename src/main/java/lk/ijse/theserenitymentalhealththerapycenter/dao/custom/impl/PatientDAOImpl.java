package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.GenericDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class PatientDAOImpl  implements PatientDAO {

    public void save(Patient entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.persist(entity);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        }
    }

    public void update(Patient entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Patient existing = session.get(Patient.class, entity.getId());
                if (existing == null) {
                    throw new IllegalArgumentException("Patient not found with ID: " + entity.getId());
                }
                entity.setName(entity.getName());
                entity.setEmail(entity.getEmail());
                entity.setPhone(entity.getPhone());
                entity.setAddress(entity.getAddress());
                entity.setInterviewNote(entity.getInterviewNote());

//                session.merge(entity);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        }
    }

    public void delete(Patient entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Patient merged = session.merge(entity);
                session.remove(merged);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                throw e;
            }
        }
    }

    public Patient getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(Patient.class, id);
        }
    }

    public void save(Patient entity, Session session) {
        session.persist(entity);
    }

    public void update(Patient entity, Session session) {
        session.merge(entity);
    }

    public Patient getById(Object id, Session session) {
        return session.get(Patient.class, id);
    }

    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("SELECT COUNT(e) FROM " + Patient.class.getSimpleName() + " e", Long.class)
                    .uniqueResult();
        }
    }

    @Override
    public List<Patient> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM Patient ", Patient.class)
                    .setCacheable(true)
                    .list();


        }
    }

    @Override
    public List<Patient> searchByName(String name) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE LOWER(p.name) LIKE LOWER(:name)", Patient.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }

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

    public List<Patient> getAllWithPrograms() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.patientTherapyPrograms ptp LEFT JOIN FETCH ptp.program", Patient.class
            ).list();
        }
    }

    public Patient findByPhone(String phone) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE p.phone = :phone", Patient.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        }
    }
}
