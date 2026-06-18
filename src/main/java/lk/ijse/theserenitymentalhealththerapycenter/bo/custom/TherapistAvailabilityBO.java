package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistAvailabilityDTO;

import java.util.List;

public interface TherapistAvailabilityBO extends SuperBO {
    List<TherapistAvailabilityDTO> getAllAvailabilityByTherapist(Long id);
    void saveAvailability(TherapistAvailabilityDTO therapistAvailabilityDTO);
    void deleteAvailabilityById(Long id);
    boolean updateAvailability(TherapistAvailabilityDTO therapistAvailabilityDTO);

}
