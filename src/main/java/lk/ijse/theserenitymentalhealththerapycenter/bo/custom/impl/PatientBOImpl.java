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

    private final lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl therapySessionDAO = new lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl();

    /**
     * Register a new patient with validation and generate sessions for their programs.
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
        
        // Use a transaction since we are saving a patient and their programs
        // We'll just save the patient first to get the generated ID
        for (TherapyProgram program : patient.getPrograms()) {
            p.getPrograms().add(program);
        }
        
        patientDAO.save(p);

        // Generate sessions for each enrolled program
        List<lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession> generatedSessions = new java.util.ArrayList<>();
        for (TherapyProgram program : patient.getPrograms()) {
            int numSessions = (program.getTotalSessions() != null) ? program.getTotalSessions() : 1;
            
            for (int i = 1; i <= numSessions; i++) {
                lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession session = new lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession();
                session.setPatient(p);
                session.setProgram(program);
                session.setSequenceNumber(i);
                session.setStatus(lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.SessionStatus.UNSCHEDULED);
                session.setPaymentStatus(lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.PaymentStatus.PENDING);
                
                generatedSessions.add(session);
            }
        }
        
        if (!generatedSessions.isEmpty()) {
            therapySessionDAO.saveAll(generatedSessions);
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
}
