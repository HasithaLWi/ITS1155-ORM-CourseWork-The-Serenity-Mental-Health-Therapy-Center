package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class PatientDAOImpl implements PatientDAO {


    @Override
    public void save(Patient entity) {
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
    public void update(Patient entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Patient existing = session.get(Patient.class, entity.getId());
                if (existing != null) {
                    copyFields(entity, existing);
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
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Patient existing = session.get(Patient.class, entity.getId());
                session.remove(existing);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
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
            return session.createQuery("SELECT COUNT(e) FROM " + Patient.class.getSimpleName() + " e", Long.class)
                    .uniqueResult();
        }
    }



    @Override
    public void save(Patient entity, Session session) {
        session.persist(entity);
    }

    @Override
    public void update(Patient entity, Session session) {
        Patient existing = session.get(Patient.class, entity.getId());
        if (existing != null) {
            copyFields(entity, existing);
        }
    }

    @Override
    public void delete(Patient entity, Session session) {
        Patient merged = session.merge(entity);
        session.remove(merged);
    }

    @Override
    public Patient getById(Object id, Session session) {
        return session.get(Patient.class, id);
    }

    @Override
    public List<Patient> getAll(Session session) {
        return session.createQuery("FROM Patient", Patient.class).list();
    }

    @Override
    public long count(Session session) {
        return session.createQuery("SELECT COUNT(e) FROM Patient e", Long.class)
                .uniqueResult();
    }



    @Override
    public List<Patient> searchByName(String name) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE LOWER(p.firstName) LIKE LOWER(:name) OR LOWER(p.lastName) LIKE LOWER(:name)", Patient.class);
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

    @Override
    public Patient findByEmail(String email) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Patient> query = session.createQuery(
                    "FROM Patient p WHERE p.email = :email", Patient.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }

    @Override
    public List<Patient> getPatientsWithNoScheduledSessions() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Patient p WHERE p.id NOT IN (" +
                    "  SELECT DISTINCT s.patient.id FROM TherapySession s WHERE s.status = :status" +
                    ")", Patient.class)
                    .setParameter("status", lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.SessionStatus.SCHEDULED)
                    .list();
        }
    }

    /**
     * Copy all patient fields from source to target entity (used in update operations).
     */
    private void copyFields(Patient source, Patient target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setGenderIdentity(source.getGenderIdentity());
        target.setEmail(source.getEmail());
        target.setPhone(source.getPhone());
        target.setAddress(source.getAddress());
        target.setEmergencyContactName(source.getEmergencyContactName());
        target.setEmergencyContactRelationship(source.getEmergencyContactRelationship());
        target.setEmergencyContactPhone(source.getEmergencyContactPhone());
        target.setInsuranceProvider(source.getInsuranceProvider());
        target.setInsurancePolicyId(source.getInsurancePolicyId());
        target.setInsuranceGroupNumber(source.getInsuranceGroupNumber());
        target.setStatus(source.getStatus());
        target.setInterviewNote(source.getInterviewNote());
    }
}
