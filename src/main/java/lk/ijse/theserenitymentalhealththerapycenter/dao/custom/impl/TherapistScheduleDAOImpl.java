package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistScheduleDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapistSchedule;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class TherapistScheduleDAOImpl implements TherapistScheduleDAO {
    @Override
    public List<TherapistSchedule> getAllByTherapistId(long therapistId) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM TherapistSchedule  WHERE therapist.id = :therapistId", TherapistSchedule.class)
                    .setParameter("therapistId", therapistId)
                    .setCacheable(true)
                    .list();
        }
    }

    @Override
    public List<TherapistSchedule> getAllByTherapistIdAndDate(long therapistId, String date) {
        return List.of();
    }

    @Override
    public List<TherapistSchedule> getAllByTherapistIdAndDateRange(long therapistId, String startDate, String endDate) {
        return List.of();
    }

    @Override
    public List<TherapistSchedule> getAllByTherapistIdAndScheduleType(long therapistId, String scheduleType) {
        return List.of();
    }

    @Override
    public List<TherapistSchedule> getAllByTherapistIdAndScheduleTypeAndDateRange(long therapistId, String scheduleType, String startDate, String endDate) {
        return List.of();
    }

    @Override
    public void save(TherapistSchedule entity) {

    }

    @Override
    public void update(TherapistSchedule entity) {

    }

    @Override
    public void delete(TherapistSchedule entity) {

    }

    @Override
    public TherapistSchedule getById(Object id) {
        return null;
    }

    @Override
    public List<TherapistSchedule> getAll() {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void save(TherapistSchedule entity, Session session) {

        session.persist(entity);

    }

    @Override
    public void update(TherapistSchedule entity, Session session) {
        TherapistSchedule existing = session.get(TherapistSchedule.class, entity.getId());
        if (existing != null) {
            existing.setDate(entity.getDate());
            existing.setTime(entity.getTime());
            existing.setScheduleType(entity.getScheduleType());
            existing.setTherapist(entity.getTherapist());
        }
        session.merge(existing);

    }

    @Override
    public void delete(TherapistSchedule entity, Session session) {

    }

    @Override
    public TherapistSchedule getById(Object id, Session session) {
        return session.get(TherapistSchedule.class, id);
    }

    @Override
    public List<TherapistSchedule> getAll(Session session) {
        return List.of();
    }

    @Override
    public long count(Session session) {
        return 0;
    }
}
