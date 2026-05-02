package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TherapyProgramDAO extends GenericDAO<TherapyProgram> {

    public TherapyProgramDAO() {
        super(TherapyProgram.class);
    }

    /**
     * Search programs by name (case-insensitive LIKE query).
     */
    public List<TherapyProgram> searchByName(String name) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<TherapyProgram> query = session.createQuery(
                    "FROM TherapyProgram p WHERE LOWER(p.name) LIKE LOWER(:name)", TherapyProgram.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }
}
