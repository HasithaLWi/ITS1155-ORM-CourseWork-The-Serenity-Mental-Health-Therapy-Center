package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientTherapyProgramDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientBOImpl implements PatientBO {
    private final PatientDAOImpl patientDAO = new PatientDAOImpl();
    private final PatientTherapyProgramDAOImpl ptpDAO = new PatientTherapyProgramDAOImpl();

    /**
     * Register a new patient with validation.
     * Creates PatientTherapyProgram records with upfront credit — NO sessions are generated.
     * Returns the generated patient ID.
     */
    public Long registerPatient(PatientDTO patient) {
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
        p.setInterviewNote(patient.getInterviewNote());
        
        // Save patient first to get the generated ID
        patientDAO.save(p);

        // Create PatientTherapyProgram records with upfront credit (NO sessions generated)
        List<PatientTherapyProgram> enrollments = new ArrayList<>();
        Map<Long, Integer> upfrontMap = patient.getUpfrontSessionsPerProgram();

        for (TherapyProgram program : patient.getPrograms()) {
            int upfrontSessions = 0;
            if (upfrontMap != null && upfrontMap.containsKey(program.getId())) {
                upfrontSessions = upfrontMap.get(program.getId());
            }
            PatientTherapyProgram ptp = new PatientTherapyProgram(p, program, upfrontSessions);
            enrollments.add(ptp);
        }

        if (!enrollments.isEmpty()) {
            ptpDAO.saveAll(enrollments);
        }

        return p.getId();
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

    /**
     * Get all program enrollments for a patient (with credit info).
     */
    public List<PatientTherapyProgram> getPatientPrograms(Long patientId) {
        return ptpDAO.findByPatient(patientId);
    }

    /**
     * Get specific patient-program enrollment.
     */
    public PatientTherapyProgram getPatientProgram(Long patientId, Long programId) {
        return ptpDAO.findByPatientAndProgram(patientId, programId);
    }

    /**
     * Deduct one upfront credit for a patient-program enrollment.
     */
    public void deductUpfrontCredit(Long patientId, Long programId) {
        ptpDAO.deductCredit(patientId, programId);
    }
}
