package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class TherapistDAOImpl implements TherapistDAO {



    @Override
    public void save(Therapist entity) {
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
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Therapist merged = session.merge(entity);
                session.remove(merged);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public Therapist getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(Therapist.class, id);
        }
    }

    @Override
    public List<Therapist> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM Therapist", Therapist.class).list();
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("SELECT COUNT(e) FROM Therapist e", Long.class)
                    .uniqueResult();
        }
    }



    @Override
    public void save(Therapist entity, Session session) {
        session.persist(entity);
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
        Therapist merged = session.merge(entity);
        session.remove(merged);
    }

    @Override
    public Therapist getById(Object id, Session session) {
        return session.get(Therapist.class, id);
    }

    @Override
    public List<Therapist> getAll(Session session) {
        return session.createQuery("FROM Therapist", Therapist.class).list();
    }

    @Override
    public long count(Session session) {
        return session.createQuery("SELECT COUNT(e) FROM Therapist e", Long.class)
                .uniqueResult();
    }



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
    public long countByStatus(Therapist.Status status) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(t) FROM Therapist t WHERE t.status = :status", Long.class)
                    .setParameter("status", status)
                    .uniqueResult();
            return count != null ? count : 0;
        }
    }

    @Override
    public Therapist findByPhone(String phone) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE t.phone = :phone", Therapist.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        }
    }

    @Override
    public Therapist findByEmail(String email) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE t.email = :email", Therapist.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }

    @Override
    public Therapist findByName(String name) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE t.name = :name", Therapist.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        }
    }
}
