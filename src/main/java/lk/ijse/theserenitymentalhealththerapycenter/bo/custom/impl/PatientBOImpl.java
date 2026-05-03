package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;

public class PatientBOImpl implements PatientBO {
    private final PatientDAOImpl patientDAO = new PatientDAOImpl();

    /**
     * Register a new patient with validation.
     */
    public void registerPatient(PatientDTO patient) {
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

        Patient p = new Patient();
        p.setName(patient.getName());
        p.setEmail(patient.getEmail());
        p.setAddress(patient.getAddress());
        p.setPhone(patient.getPhone());
        for (TherapyProgram program : patient.getPrograms()) {
            PatientTherapyProgram therapyProgram = new PatientTherapyProgram();
            therapyProgram.setPatient(p);
            therapyProgram.setProgram(program);
            p.getPrograms().add(program);
        }
        patientDAO.save(p);
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
