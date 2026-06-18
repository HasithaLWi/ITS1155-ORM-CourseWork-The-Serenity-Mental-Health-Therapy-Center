package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistScheduleBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistScheduleDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistScheduleDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapistAvailability;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapistSchedule;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TherapistScheduleBOImpl implements TherapistScheduleBO {
    private final TherapistScheduleDAO therapistScheduleDAO = (TherapistScheduleDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPIST_SCHEDULE);
    private final TherapistDAO therapistDAO = (TherapistDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPIST);
    @Override
    public void saveTherapistSchedule(TherapistScheduleDTO dto) {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            therapistScheduleDAO.save(new TherapistSchedule(
                    dto.getId(),
                    dto.getScheduleType(),
                    dto.getDate(),
                    dto.getTime(),
                    therapistDAO.getById(dto.getTherapistId())

            ), session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }

    }

    @Override
    public void updateTherapistSchedule(TherapistScheduleDTO dto) {

    }

    @Override
    public void deleteTherapistSchedule(Long id) {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapistSchedule entity = therapistScheduleDAO.getById(id, session);
            therapistScheduleDAO.delete(entity, session);
            transaction.commit();

        }  catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }

    }

    @Override
    public List<TherapistScheduleDTO> getAllSchedulesByTherapist(long therapistId) {
        List<TherapistScheduleDTO> dtos;
        dtos = therapistScheduleDAO.getAllByTherapistId(therapistId).stream().map(schedule -> new TherapistScheduleDTO(
                schedule.getId(),
                schedule.getScheduleType(),
                schedule.getDate(),
                schedule.getTime(),
                schedule.getTherapist().getId()
        )).toList();
        return dtos;
    }

    @Override
    public long getTherapistScheduleCount() {
        return 0;
    }

    @Override
    public TherapistScheduleDTO getTherapistScheduleById(Long id) {
        return null;
    }
}
