package lk.ijse.theserenitymentalhealththerapycenter.bo;

import lk.ijse.theserenitymentalhealththerapycenter.dao.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;

public class PatientService {
    private final PatientDAO patientDAO = new PatientDAO();

    /**
     * Register a new patient with validation.
     */
    public void registerPatient(Patient patient) {
        if (!ValidationUtil.isValidName(patient.getName())) {
            throw new RegistrationException("Valid patient name is required.");
        }
        if (patient.getEmail() != null && !patient.getEmail().isEmpty()
                && !ValidationUtil.isValidEmail(patient.getEmail())) {
            throw new RegistrationException("Invalid email format.");
        }
        if (patient.getPhone() != null && !patient.getPhone().isEmpty()
                && !ValidationUtil.isValidPhone(patient.getPhone())) {
            throw new RegistrationException("Invalid phone number format.");
        }

        patientDAO.save(patient);
    }

    public void updatePatient(Patient patient) {
        patientDAO.update(patient);
    }

    public void deletePatient(Patient patient) {
        patientDAO.delete(patient);
    }

    public Patient getPatientById(Long id) {
        return patientDAO.getById(id);
    }

    public List<Patient> getAllPatients() {
        return patientDAO.getAll();
    }

    public List<Patient> searchPatients(String name) {
        return patientDAO.searchByName(name);
    }

    public List<Patient> getAllWithPrograms() {
        return patientDAO.getAllWithPrograms();
    }

    public List<Patient> findPatientsInAllPrograms() {
        return patientDAO.findPatientsInAllPrograms();
    }

    public long getPatientCount() {
        return patientDAO.count();
    }
}
