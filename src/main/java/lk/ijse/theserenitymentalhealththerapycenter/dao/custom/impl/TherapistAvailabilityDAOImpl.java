package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistAvailabilityDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapistAvailability;
import org.hibernate.Session;

import java.util.List;

public class TherapistAvailabilityDAOImpl implements TherapistAvailabilityDAO {
    @Override
    public List<TherapistAvailability> getAllByTherapist(long id) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM TherapistAvailability WHERE therapist.id = :therapistId", TherapistAvailability.class)
                    .setParameter("therapistId", id)
                    .getResultList();
        }
    }

    @Override
    public void save(TherapistAvailability entity) {


    }

    @Override
    public void update(TherapistAvailability entity) {

    }

    @Override
    public void delete(TherapistAvailability entity) {

    }

    @Override
    public TherapistAvailability getById(Object id) {
        return null;
    }

    @Override
    public List<TherapistAvailability> getAll() {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void save(TherapistAvailability entity, Session session) {

        session.persist(entity);

    }

    @Override
    public void update(TherapistAvailability entity, Session session) {
            TherapistAvailability existing = session.get(TherapistAvailability.class, entity.getId());
            if (existing != null) {
                existing.setDayOfWeek(entity.getDayOfWeek());
                existing.setTime(entity.getTime());
                existing.setTherapist(entity.getTherapist());
            }
            session.merge(existing);

    }

    @Override
    public void delete(TherapistAvailability entity, Session session) {

        session.remove(entity);

    }

    @Override
    public TherapistAvailability getById(Object id, Session session) {

        return session.get(TherapistAvailability.class, id);
    }

    @Override
    public List<TherapistAvailability> getAll(Session session) {
        return List.of();
    }

    @Override
    public long count(Session session) {
        return 0;
    }
}
