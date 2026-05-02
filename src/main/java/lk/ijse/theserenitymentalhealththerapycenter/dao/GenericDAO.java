package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class GenericDAO<T> {
    private final Class<T> type;

    public GenericDAO(Class<T> type) {
        this.type = type;
    }

    public void save(T entity) {
        Transaction transaction = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void update(T entity) {
        Transaction transaction = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            transaction = session.beginTransaction();
            T merged = session.merge(entity);
            session.remove(merged);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public T getById(Object id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(type, id);
        }
    }

    public List<T> getAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {

          return session.createQuery("FROM " + type.getSimpleName(), type).list();

//            // Dynamically generate the region name (e.g., "PatientListCache", "TherapistListCache")
//            String regionName = type.getSimpleName() + "ListCache";
//
//            return session.createQuery("FROM " + type.getSimpleName(), type)
//                    .setCacheable(true)
//                    .setCacheRegion(regionName) // <-- Pass the dynamic variable here
//                    .list();
        }
    }

    public long count() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("SELECT COUNT(e) FROM " + type.getSimpleName() + " e", Long.class)
                    .uniqueResult();
        }
    }
}
