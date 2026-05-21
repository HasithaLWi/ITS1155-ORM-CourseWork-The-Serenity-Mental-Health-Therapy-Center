package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;

import java.util.List;

public interface TherapistDAO extends CrudDAO<Therapist> {
    List<Therapist> searchByName(String name);
    List<Therapist> findByStatus(Therapist.Status status);
    List<Therapist> findBySpecialty(String specialty);
}
