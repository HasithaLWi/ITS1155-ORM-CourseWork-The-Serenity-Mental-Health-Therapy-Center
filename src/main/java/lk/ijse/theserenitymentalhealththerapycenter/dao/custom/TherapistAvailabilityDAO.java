package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapistAvailability;

import java.util.List;

public interface TherapistAvailabilityDAO extends CrudDAO<TherapistAvailability> {
    List<TherapistAvailability> getAllByTherapist(long id);
}
