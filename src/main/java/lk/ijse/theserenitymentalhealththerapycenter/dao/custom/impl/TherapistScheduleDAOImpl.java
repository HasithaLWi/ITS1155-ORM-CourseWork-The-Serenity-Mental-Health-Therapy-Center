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
    public void save(TherapistScheduleDAO entity) {

    }

    @Override
    public void update(TherapistScheduleDAO entity) {

    }

    @Override
    public void delete(TherapistScheduleDAO entity) {

    }

    @Override
    public TherapistScheduleDAO getById(Object id) {
        return null;
    }

    @Override
    public List<TherapistScheduleDAO> getAll() {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void save(TherapistScheduleDAO entity, Session session) {

    }

    @Override
    public void update(TherapistScheduleDAO entity, Session session) {

    }

    @Override
    public void delete(TherapistScheduleDAO entity, Session session) {

    }

    @Override
    public TherapistScheduleDAO getById(Object id, Session session) {
        return null;
    }

    @Override
    public List<TherapistScheduleDAO> getAll(Session session) {
        return List.of();
    }

    @Override
    public long count(Session session) {
        return 0;
    }
}
