package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.UserDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserDAOImpl implements UserDAO {



    @Override
    public void save(User entity) {
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
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                User merged = session.merge(entity);
                session.remove(merged);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public User getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(User.class, id);
        }
    }

    @Override
    public List<User> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM User", User.class).list();
        }
    }

    @Override
    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("SELECT COUNT(e) FROM User e", Long.class)
                    .uniqueResult();
        }
    }



    @Override
    public void save(User entity, Session session) {
        session.persist(entity);
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
        User merged = session.merge(entity);
        session.remove(merged);
    }

    @Override
    public User getById(Object id, Session session) {
        return session.get(User.class, id);
    }

    @Override
    public List<User> getAll(Session session) {
        return session.createQuery("FROM User", User.class).list();
    }

    @Override
    public long count(Session session) {
        return session.createQuery("SELECT COUNT(e) FROM User e", Long.class)
                .uniqueResult();
    }



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
