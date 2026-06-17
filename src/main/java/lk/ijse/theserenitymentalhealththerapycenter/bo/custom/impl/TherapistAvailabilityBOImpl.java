package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistAvailabilityBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistAvailabilityDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistAvailabilityDTO;

import java.util.ArrayList;
import java.util.List;

public class TherapistAvailabilityBOImpl implements TherapistAvailabilityBO {
    private final TherapistAvailabilityDAO therapistAvailabilityDAO = (TherapistAvailabilityDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.THERAPIST_AVAILABILITY);

    @Override
    public List<TherapistAvailabilityDTO> getAllAvailabilityByTherapist(Long id) {
        return therapistAvailabilityDAO.getAllByTherapist(id).stream()
                .map(availability -> new TherapistAvailabilityDTO(
                        availability.getId(),
                        availability.getDate(),
                        availability.getTime(),
                        availability.getTherapist().getId()
                )).toList();
    }

    @Override
    public void saveAvailability(TherapistAvailabilityDTO therapistAvailabilityDTO) {

    }

    @Override
    public boolean deleteAvailabilityById(Long id) {
        return false;
    }

    @Override
    public boolean updateAvailability(TherapistAvailabilityDTO therapistAvailabilityDTO) {
        return false;
    }
}
