package lk.ijse.theserenitymentalhealththerapycenter.dao;

import org.hibernate.Session;

import java.util.List;

public interface CrudDAO<T> extends SuperDAO {

    // ==================== Self-Contained ====================
    void save(T entity);
    void update(T entity);
    void delete(T entity);
    T getById(Object id);
    List<T> getAll();
    long count();

    // ==================== Session-Aware ====================
    void save(T entity, Session session);
    void update(T entity, Session session);
    void delete(T entity, Session session);
    T getById(Object id, Session session);
    List<T> getAll(Session session);
    long count(Session session);
}
