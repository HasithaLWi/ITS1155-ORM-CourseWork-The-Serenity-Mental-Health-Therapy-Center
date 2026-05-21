package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudUtil;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class TherapistDAOImpl implements TherapistDAO {

    // ==================== CrudDAO: Self-Contained ====================

    @Override
    public void save(Therapist entity) {
        CrudUtil.save(entity);
    }

    @Override
    public void update(Therapist entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Therapist existing = session.get(Therapist.class, entity.getId());
                if (existing != null) {
                    existing.setName(entity.getName());
                    existing.setSpecialty(entity.getSpecialty());
                    existing.setPhone(entity.getPhone());
                    existing.setEmail(entity.getEmail());
                    existing.setStatus(entity.getStatus());
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(Therapist entity) {
        CrudUtil.delete(entity);
    }

    @Override
    public Therapist getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getById(Therapist.class, id, session);
        }
    }

    @Override
    public List<Therapist> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getAll(Therapist.class, session);
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.count(Therapist.class, session);
        }
    }

    // ==================== CrudDAO: Session-Aware ====================

    @Override
    public void save(Therapist entity, Session session) {
        CrudUtil.save(entity, session);
    }

    @Override
    public void update(Therapist entity, Session session) {
        Therapist existing = session.get(Therapist.class, entity.getId());
        if (existing != null) {
            existing.setName(entity.getName());
            existing.setSpecialty(entity.getSpecialty());
            existing.setPhone(entity.getPhone());
            existing.setEmail(entity.getEmail());
            existing.setStatus(entity.getStatus());
        }
    }

    @Override
    public void delete(Therapist entity, Session session) {
        CrudUtil.delete(entity, session);
    }

    @Override
    public Therapist getById(Object id, Session session) {
        return CrudUtil.getById(Therapist.class, id, session);
    }

    @Override
    public List<Therapist> getAll(Session session) {
        return CrudUtil.getAll(Therapist.class, session);
    }

    @Override
    public long count(Session session) {
        return CrudUtil.count(Therapist.class, session);
    }

    // ==================== Custom Methods ====================

    @Override
    public List<Therapist> searchByName(String name) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE LOWER(t.name) LIKE LOWER(:name)", Therapist.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }

    @Override
    public List<Therapist> findByStatus(Therapist.Status status) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE t.status = :status", Therapist.class);
            query.setParameter("status", status);
            return query.list();
        }
    }

    @Override
    public List<Therapist> findBySpecialty(String specialty) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE LOWER(t.specialty) LIKE LOWER(:specialty)", Therapist.class);
            query.setParameter("specialty", "%" + specialty + "%");
            return query.list();
        }
    }
}
