package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistScheduleDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapistSchedule;

import java.util.List;

public interface TherapistScheduleDAO extends CrudDAO<TherapistScheduleDAO> {
    List<TherapistSchedule> getAllByTherapistId(long therapistId);
     List<TherapistSchedule> getAllByTherapistIdAndDate(long therapistId, String date);
     List<TherapistSchedule> getAllByTherapistIdAndDateRange(long therapistId, String startDate, String endDate);
     List<TherapistSchedule> getAllByTherapistIdAndScheduleType(long therapistId, String scheduleType);
     List<TherapistSchedule> getAllByTherapistIdAndScheduleTypeAndDateRange(long therapistId, String scheduleType, String startDate, String endDate);
}
