package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudUtil;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class PatientDAOImpl implements PatientDAO {

    // ==================== CrudDAO: Self-Contained ====================

    @Override
    public void save(Patient entity) {
        CrudUtil.save(entity);
    }

    @Override
    public void update(Patient entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Patient existing = session.get(Patient.class, entity.getId());
                if (existing != null) {
                    existing.setName(entity.getName());
                    existing.setEmail(entity.getEmail());
                    existing.setPhone(entity.getPhone());
                    existing.setAddress(entity.getAddress());
                    existing.setInterviewNote(entity.getInterviewNote());
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(Patient entity) {
        CrudUtil.delete(entity);
    }

    @Override
    public Patient getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(Patient.class, id);
        }
    }

    @Override
    public List<Patient> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM Patient", Patient.class)
                    .setCacheable(true)
                    .list();
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.count(Patient.class, session);
        }
    }

    // ==================== CrudDAO: Session-Aware ====================

    @Override
    public void save(Patient entity, Session session) {
        CrudUtil.save(entity, session);
    }

    @Override
    public void update(Patient entity, Session session) {
        Patient existing = session.get(Patient.class, entity.getId());
        if (existing != null) {
            existing.setName(entity.getName());
            existing.setEmail(entity.getEmail());
            existing.setPhone(entity.getPhone());
            existing.setAddress(entity.getAddress());
            existing.setInterviewNote(entity.getInterviewNote());
        }
    }

    @Override
    public void delete(Patient entity, Session session) {
        CrudUtil.delete(entity, session);
    }

    @Override
    public Patient getById(Object id, Session session) {
        return session.get(Patient.class, id);
    }

    @Override
    public List<Patient> getAll(Session session) {
        return CrudUtil.getAll(Patient.class, session);
    }

    @Override
    public long count(Session session) {
        return CrudUtil.count(Patient.class, session);
    }

    // ==================== Custom Methods ====================

    @Override
    public List<Patient> searchByName(String name) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE LOWER(p.name) LIKE LOWER(:name)", Patient.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }

    @Override
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

    @Override
    public List<Patient> getAllWithPrograms() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.patientTherapyPrograms ptp LEFT JOIN FETCH ptp.program", Patient.class
            ).list();
        }
    }

    @Override
    public Patient findByPhone(String phone) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE p.phone = :phone", Patient.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        }
    }
}
