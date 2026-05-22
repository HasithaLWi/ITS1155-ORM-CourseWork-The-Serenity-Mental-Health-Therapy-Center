package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;

import java.util.List;

public interface TherapistBO extends SuperBO {
    void saveTherapist(TherapistDTO dto);
    void updateTherapist(TherapistDTO dto);
    void deleteTherapist(Long id);
    List<TherapistDTO> getAllTherapists();
    long getTherapistCount();
    long getActiveTherapistCount();
    TherapistDTO getTherapistById(Long id);
}
