package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapistDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SerenityException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;

public class TherapistBOImpl implements TherapistBO {
    private final TherapistDAOImpl therapistDAO = new TherapistDAOImpl();

    public void saveTherapist(Therapist therapist) {
        validateTherapist(therapist);
        therapistDAO.save(therapist);
    }

    public void updateTherapist(Therapist therapist) {
        validateTherapist(therapist);
        therapistDAO.update(therapist);
    }

    public void deleteTherapist(Therapist therapist) {
        therapistDAO.delete(therapist);
    }

    public Therapist getTherapistById(Long id) {
        return therapistDAO.getById(id);
    }

    public List<Therapist> getAllTherapists() {
        return therapistDAO.getAll();
    }

    public List<Therapist> searchTherapists(String name) {
        return therapistDAO.searchByName(name);
    }

    public List<Therapist> getActiveTherapists() {
        return therapistDAO.findByStatus(Therapist.Status.ACTIVE);
    }

    public long getTherapistCount() {
        return therapistDAO.count();
    }

    private void validateTherapist(Therapist therapist) {
        if (!ValidationUtil.isValidName(therapist.getName())) {
            throw new SerenityException("Valid therapist name is required.");
        }
        if (therapist.getEmail() != null && !therapist.getEmail().isEmpty()
                && !ValidationUtil.isValidEmail(therapist.getEmail())) {
            throw new SerenityException("Invalid email format.");
        }
        if (therapist.getPhone() != null && !therapist.getPhone().isEmpty()
                && !ValidationUtil.isValidPhone(therapist.getPhone())) {
            throw new SerenityException("Invalid phone number format.");
        }
    }
}
