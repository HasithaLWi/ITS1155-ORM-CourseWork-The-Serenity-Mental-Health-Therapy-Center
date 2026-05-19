package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapistDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.TherapistStatus;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SerenityException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;

public class TherapistBOImpl implements TherapistBO {
    private final TherapistDAOImpl therapistDAO = new TherapistDAOImpl();

    public void saveTherapist(TherapistDTO dto) {
        validateTherapist(dto);
        therapistDAO.save(toEntity(dto));
    }

    public void updateTherapist(TherapistDTO dto) {
        validateTherapist(dto);
        therapistDAO.update(toEntity(dto));
    }

    public void deleteTherapist(Long id) {
        Therapist entity = therapistDAO.getById(id);
        if (entity == null) throw new SerenityException("Therapist not found.");
        therapistDAO.delete(entity);
    }

    public TherapistDTO getTherapistById(Long id) {
        Therapist entity = therapistDAO.getById(id);
        if (entity == null) throw new SerenityException("Therapist not found.");
        return toDTO(entity);
    }

    public List<TherapistDTO> getAllTherapists() {
        return therapistDAO.getAll().stream().map(this::toDTO).toList();
    }

    public List<TherapistDTO> searchTherapists(String name) {
        return therapistDAO.searchByName(name).stream().map(this::toDTO).toList();
    }

    public List<TherapistDTO> getActiveTherapists() {
        return therapistDAO.findByStatus(Therapist.Status.ACTIVE).stream().map(this::toDTO).toList();
    }

    public long getTherapistCount() {
        return therapistDAO.count();
    }

    // ==================== Conversion Helpers ====================

    private TherapistDTO toDTO(Therapist entity) {
        TherapistDTO dto = new TherapistDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSpecialty(entity.getSpecialty());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setStatus(entity.getStatus() != null ? TherapistStatus.valueOf(entity.getStatus().name()) : null);
        return dto;
    }

    private Therapist toEntity(TherapistDTO dto) {
        Therapist entity = new Therapist();
        if (dto.getId() > 0) {
            entity.setId(dto.getId());
        }
        entity.setName(dto.getName());
        entity.setSpecialty(dto.getSpecialty());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setStatus(dto.getStatus() != null ? Therapist.Status.valueOf(dto.getStatus().name()) : Therapist.Status.ACTIVE);
        return entity;
    }

    private void validateTherapist(TherapistDTO dto) {
        if (!ValidationUtil.isValidName(dto.getName())) {
            throw new SerenityException("Valid therapist name is required.");
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()
                && !ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new SerenityException("Invalid email format.");
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()
                && !ValidationUtil.isValidPhone(dto.getPhone())) {
            throw new SerenityException("Invalid phone number format.");
        }
    }
}
