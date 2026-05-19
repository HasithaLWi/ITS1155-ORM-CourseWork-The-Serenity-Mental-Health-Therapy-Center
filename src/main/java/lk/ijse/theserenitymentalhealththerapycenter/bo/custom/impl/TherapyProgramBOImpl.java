package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapyProgramDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientTherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.TherapistStatus;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SerenityException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;

public class TherapyProgramBOImpl implements TherapyProgramBO {
    private final TherapyProgramDAOImpl programDAO = new TherapyProgramDAOImpl();

    public void saveProgram(TherapyProgramDTO dto) {
        validateProgram(dto);
        programDAO.save(toEntity(dto));
    }

    public void updateProgram(TherapyProgramDTO dto) {
        validateProgram(dto);
        programDAO.update(toEntity(dto));
    }

    public void deleteProgram(Long id) {
        TherapyProgram entity = programDAO.getById(id);
        if (entity == null) throw new SerenityException("Program not found.");
        programDAO.delete(entity);
    }

    public TherapyProgramDTO getProgramById(Long id) {
        TherapyProgram entity = programDAO.getById(id);
        if (entity == null) {
            throw new SerenityException("Program not found with ID: " + id);
        }
        return toDTO(entity);
    }

    public List<TherapyProgramDTO> getAllPrograms() {
        return programDAO.getAll().stream().map(this::toDTO).toList();
    }

    public List<TherapyProgramDTO> searchPrograms(String name) {
        return programDAO.searchByName(name).stream().map(this::toDTO).toList();
    }

    public long getProgramCount() {
        return programDAO.count();
    }

    // ==================== Conversion Helpers ====================

    public TherapyProgramDTO toDTO(TherapyProgram entity) {
        TherapyProgramDTO dto = new TherapyProgramDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDuration(entity.getDuration());
        dto.setFee(entity.getFee());
        dto.setTotalSessions(entity.getTotalSessions());
        dto.setSessionFee(entity.getSessionFee());
        dto.setDescription(entity.getDescription());
        dto.setTherapists(entity.getTherapists().stream().map(t ->
                new TherapistDTO(
                        t.getId(),
                        t.getName(),
                        t.getSpecialty(),
                        t.getPhone(),
                        t.getEmail(),
                        t.getStatus().equals(Therapist.Status.ACTIVE) ? TherapistStatus.ACTIVE : TherapistStatus.INACTIVE
                        )).toList());
        return dto;
    }

    public TherapyProgram toEntity(TherapyProgramDTO dto) {
        TherapyProgram entity = new TherapyProgram();
        if (dto.getId() > 0) {
            entity.setId(dto.getId());
        }
        entity.setName(dto.getName());
        entity.setDuration(dto.getDuration());
        entity.setFee(dto.getFee());
        entity.setTotalSessions(dto.getTotalSessions());
        entity.setSessionFee(dto.getSessionFee());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    private void validateProgram(TherapyProgramDTO program) {
        if (!ValidationUtil.isNotEmpty(program.getName())) {
            throw new SerenityException("Program name is required.");
        }
        if (program.getFee() != null && program.getFee().signum() < 0) {
            throw new SerenityException("Program fee cannot be negative.");
        }
    }
}
