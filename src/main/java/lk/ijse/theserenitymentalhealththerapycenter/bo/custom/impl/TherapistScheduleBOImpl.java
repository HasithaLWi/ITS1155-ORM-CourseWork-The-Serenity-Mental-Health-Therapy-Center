package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistScheduleBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistScheduleDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistScheduleDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapistSchedule;

import java.util.ArrayList;
import java.util.List;

public class TherapistScheduleBOImpl implements TherapistScheduleBO {
    private final TherapistScheduleDAO therapistScheduleDAO = (TherapistScheduleDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPIST_SCHEDULE);
    @Override
    public void saveTherapistSchedule(TherapistScheduleDTO dto) {

    }

    @Override
    public void updateTherapistSchedule(TherapistScheduleDTO dto) {

    }

    @Override
    public void deleteTherapistSchedule(Long id) {

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
