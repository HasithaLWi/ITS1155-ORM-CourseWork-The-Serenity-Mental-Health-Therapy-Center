package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudUtil;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.UserDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserDAOImpl implements UserDAO {

    // ==================== CrudDAO: Self-Contained ====================

    @Override
    public void save(User entity) {
        CrudUtil.save(entity);
    }

    @Override
    public void update(User entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                User existing = session.get(User.class, entity.getId());
                if (existing != null) {
                    existing.setUsername(entity.getUsername());
                    existing.setPassword(entity.getPassword());
                    existing.setFullName(entity.getFullName());
                    existing.setEmail(entity.getEmail());
                    existing.setRole(entity.getRole());
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(User entity) {
        CrudUtil.delete(entity);
    }

    @Override
    public User getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getById(User.class, id, session);
        }
    }

    @Override
    public List<User> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.getAll(User.class, session);
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return CrudUtil.count(User.class, session);
        }
    }

    // ==================== CrudDAO: Session-Aware ====================

    @Override
    public void save(User entity, Session session) {
        CrudUtil.save(entity, session);
    }

    @Override
    public void update(User entity, Session session) {
        User existing = session.get(User.class, entity.getId());
        if (existing != null) {
            existing.setUsername(entity.getUsername());
            existing.setPassword(entity.getPassword());
            existing.setFullName(entity.getFullName());
            existing.setEmail(entity.getEmail());
            existing.setRole(entity.getRole());
        }
    }

    @Override
    public void delete(User entity, Session session) {
        CrudUtil.delete(entity, session);
    }

    @Override
    public User getById(Object id, Session session) {
        return CrudUtil.getById(User.class, id, session);
    }

    @Override
    public List<User> getAll(Session session) {
        return CrudUtil.getAll(User.class, session);
    }

    @Override
    public long count(Session session) {
        return CrudUtil.count(User.class, session);
    }

    // ==================== Custom Methods ====================

    @Override
    public void createAdminUser(User adminUser) {
        if (findByUsername("admin") == null) {
            save(adminUser);
        }
    }

    @Override
    public User findByUsername(String username) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        }
    }

    @Override
    public User findByEmail(String email) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }

    @Override
    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public User findByUsernameAndEmail(String username, String email) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.username = :username AND u.email = :email", User.class);
            query.setParameter("username", username);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }
}
