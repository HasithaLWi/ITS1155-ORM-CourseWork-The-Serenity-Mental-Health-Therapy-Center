package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.ProgramSummaryTM;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class TherapyProgramDAOImpl implements TherapyProgramDAO {



    @Override
    public void save(TherapyProgram entity) {
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
    public void update(TherapyProgram entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                TherapyProgram existing = session.get(TherapyProgram.class, entity.getId());
                if (existing != null) {
                    existing.setName(entity.getName());
                    existing.setDuration(entity.getDuration());
                    existing.setFee(entity.getFee());
                    existing.setTotalSessions(entity.getTotalSessions());
                    existing.setSessionFee(entity.getSessionFee());
                    existing.setDescription(entity.getDescription());
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(TherapyProgram entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                TherapyProgram merged = session.merge(entity);
                session.remove(merged);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public TherapyProgram getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(TherapyProgram.class, id);
        }
    }

    @Override
    public List<TherapyProgram> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM TherapyProgram", TherapyProgram.class).list();
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("SELECT COUNT(e) FROM TherapyProgram e", Long.class)
                    .uniqueResult();
        }
    }



    @Override
    public void save(TherapyProgram entity, Session session) {
        session.persist(entity);
    }

    @Override
    public void update(TherapyProgram entity, Session session) {
        TherapyProgram existing = session.get(TherapyProgram.class, entity.getId());
        if (existing != null) {
            existing.setName(entity.getName());
            existing.setDuration(entity.getDuration());
            existing.setFee(entity.getFee());
            existing.setTotalSessions(entity.getTotalSessions());
            existing.setSessionFee(entity.getSessionFee());
            existing.setDescription(entity.getDescription());
        }
    }

    @Override
    public void delete(TherapyProgram entity, Session session) {
        TherapyProgram merged = session.merge(entity);
        session.remove(merged);
    }

    @Override
    public TherapyProgram getById(Object id, Session session) {
        return session.get(TherapyProgram.class, id);
    }

    @Override
    public List<TherapyProgram> getAll(Session session) {
        return session.createQuery("FROM TherapyProgram", TherapyProgram.class).list();
    }

    @Override
    public long count(Session session) {
        return session.createQuery("SELECT COUNT(e) FROM TherapyProgram e", Long.class)
                .uniqueResult();
    }

    @Override
    public List<TherapyProgram> searchByName(String name) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<TherapyProgram> query = session.createQuery(
                    "FROM TherapyProgram p WHERE LOWER(p.name) LIKE LOWER(:name)", TherapyProgram.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }
}
