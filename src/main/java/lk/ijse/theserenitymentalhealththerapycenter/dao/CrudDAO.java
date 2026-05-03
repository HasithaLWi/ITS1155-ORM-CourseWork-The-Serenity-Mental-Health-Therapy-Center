package lk.ijse.theserenitymentalhealththerapycenter.dao;

import java.util.List;

public interface CrudDAO<T> extends SuperDAO {
    void save(T entity);

    void update(T entity);

    void delete(T entity);

    T getById(Object id);

    List<T> getAll();

    long count();
}
