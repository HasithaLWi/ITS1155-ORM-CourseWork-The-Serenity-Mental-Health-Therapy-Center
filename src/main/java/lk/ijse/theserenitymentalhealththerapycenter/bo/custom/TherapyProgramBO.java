package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;

import java.util.List;

public interface TherapyProgramBO extends SuperBO {
    void saveProgram(TherapyProgramDTO dto);
    void updateProgram(TherapyProgramDTO dto);
    void deleteProgram(Long id);
    TherapyProgramDTO getProgramById(Long id);
    List<TherapyProgramDTO> getAllPrograms();
    List<TherapyProgramDTO> searchPrograms(String name);
    long getProgramCount();
}
