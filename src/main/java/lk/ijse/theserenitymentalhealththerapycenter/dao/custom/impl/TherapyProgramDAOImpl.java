package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudUtil;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class TherapyProgramDAOImpl implements TherapyProgramDAO {

    // ==================== CrudDAO: Self-Contained ====================

    @Override
    public void save(TherapyProgram entity) {
        CrudUtil.save(entity);
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
        CrudUtil.delete(entity);
    }

    @Override
    public TherapyProgram getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getById(TherapyProgram.class, id, session);
        }
    }

    @Override
    public List<TherapyProgram> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getAll(TherapyProgram.class, session);
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.count(TherapyProgram.class, session);
        }
    }

    // ==================== CrudDAO: Session-Aware ====================

    @Override
    public void save(TherapyProgram entity, Session session) {
        CrudUtil.save(entity, session);
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
        CrudUtil.delete(entity, session);
    }

    @Override
    public TherapyProgram getById(Object id, Session session) {
        return CrudUtil.getById(TherapyProgram.class, id, session);
    }

    @Override
    public List<TherapyProgram> getAll(Session session) {
        return CrudUtil.getAll(TherapyProgram.class, session);
    }

    @Override
    public long count(Session session) {
        return CrudUtil.count(TherapyProgram.class, session);
    }

    // ==================== Custom Methods ====================

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
