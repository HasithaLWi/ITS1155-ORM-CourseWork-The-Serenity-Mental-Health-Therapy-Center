package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistScheduleDTO;

import java.util.List;

public interface TherapistScheduleBO extends SuperBO {
    void saveTherapistSchedule(TherapistScheduleDTO dto);
    void updateTherapistSchedule(TherapistScheduleDTO dto);
    void deleteTherapistSchedule(Long id);
    List<TherapistScheduleDTO> getAllSchedulesByTherapist(long therapistId);
    long getTherapistScheduleCount();
    TherapistScheduleDTO getTherapistScheduleById(Long id);
}
