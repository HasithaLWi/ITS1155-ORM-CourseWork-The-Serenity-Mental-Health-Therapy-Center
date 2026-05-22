package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;


public class CrudUtil {

    private CrudUtil() {} // Prevent instantiation



    public static <T> void save(T entity, Session session) {
        session.persist(entity);
    }

    public static <T> void update(T entity, Session session) {
        session.merge(entity);
    }

    public static <T> void delete(T entity, Session session) {
        T merged = session.merge(entity);
        session.remove(merged);
    }

    public static <T> T getById(Class<T> type, Object id, Session session) {
        return session.get(type, id);
    }

    public static <T> List<T> getAll(Class<T> type, Session session) {
        return session.createQuery("FROM " + type.getSimpleName(), type).list();
    }

    public static <T> long count(Class<T> type, Session session) {
        return session.createQuery("SELECT COUNT(e) FROM " + type.getSimpleName() + " e", Long.class)
                .uniqueResult();
    }


    public static <T> void save(T entity) {
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

    public static <T> void update(T entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.merge(entity);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    public static <T> void delete(T entity) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T merged = session.merge(entity);
                session.remove(merged);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    public static <T> T getById(Class<T> type, Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(type, id);
        }
    }

    public static <T> List<T> getAll(Class<T> type) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM " + type.getSimpleName(), type).list();
        }
    }

    public static <T> long count(Class<T> type) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("SELECT COUNT(e) FROM " + type.getSimpleName() + " e", Long.class)
                    .uniqueResult();
        }
    }
}
