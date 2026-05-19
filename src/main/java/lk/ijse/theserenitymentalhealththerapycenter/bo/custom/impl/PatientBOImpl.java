package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientTherapyProgramDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientTherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
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

        for (TherapyProgramDTO programDto : patient.getPrograms()) {
            TherapyProgram programEntity = new TherapyProgram();
            programEntity.setId(programDto.getId());
            programEntity.setName(programDto.getName());
            programEntity.setDuration(programDto.getDuration());
            programEntity.setFee(programDto.getFee());
            programEntity.setTotalSessions(programDto.getTotalSessions());
            programEntity.setSessionFee(programDto.getSessionFee());
            programEntity.setDescription(programDto.getDescription());

            int upfrontSessions = 0;
            if (upfrontMap != null && upfrontMap.containsKey(programDto.getId())) {
                upfrontSessions = upfrontMap.get(programDto.getId());
            }
            
            PatientTherapyProgram ptp = new PatientTherapyProgram(p, programEntity, upfrontSessions);
            enrollments.add(ptp);
        }

        if (!enrollments.isEmpty()) {
            ptpDAO.saveAll(enrollments);
        }

        return p.getId();
    }

    public void updatePatient(PatientDTO dto) {
        Patient entity = patientDAO.getById(dto.getId());
        if (entity == null) throw new RegistrationException("Patient not found.");
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setInterviewNote(dto.getInterviewNote());
        patientDAO.update(entity);
    }

    public void deletePatient(Long id) {
        Patient entity = patientDAO.getById(id);
        if (entity == null) throw new RegistrationException("Patient not found.");
        patientDAO.delete(entity);
    }

    public PatientDTO getPatientById(Long id) {
        Patient entity = patientDAO.getById(id);
        if (entity == null) throw new RegistrationException("Patient not found.");
        return toDTO(entity);
    }

    public List<PatientDTO> getAllPatients() {
        return patientDAO.getAll().stream().map(this::toDTO).toList();
    }

    public List<PatientDTO> searchPatients(String name) {
        return patientDAO.searchByName(name).stream().map(this::toDTO).toList();
    }

    public List<PatientDTO> getAllWithPrograms() {
        return patientDAO.getAllWithPrograms().stream().map(this::toDTO).toList();
    }

    public List<PatientDTO> findPatientsInAllPrograms() {
        return patientDAO.findPatientsInAllPrograms().stream().map(this::toDTO).toList();
    }

    public long getPatientCount() {
        return patientDAO.count();
    }

    /**
     * Get all program enrollments for a patient (with credit info).
     */
    public List<PatientTherapyProgramDTO> getPatientPrograms(Long patientId) {
        return ptpDAO.findByPatient(patientId).stream().map(this::toPtpDTO).toList();
    }

    /**
     * Get specific patient-program enrollment.
     */
    public PatientTherapyProgramDTO getPatientProgram(Long patientId, Long programId) {
        PatientTherapyProgram entity = ptpDAO.findByPatientAndProgram(patientId, programId);
        return entity != null ? toPtpDTO(entity) : null;
    }

    /**
     * Deduct one upfront credit for a patient-program enrollment.
     */
    public void deductUpfrontCredit(Long patientId, Long programId) {
        ptpDAO.deductCredit(patientId, programId);
    }

    /**
     * Enroll a patient in a new program with optional upfront sessions.
     */
    public void enrollPatientInProgram(Long patientId, Long programId, int upfrontSessions) {
        Patient patient = patientDAO.getById(patientId);
        if (patient == null) throw new RegistrationException("Patient not found.");
        TherapyProgram program = new TherapyProgram();
        program.setId(programId);
        PatientTherapyProgram ptp = new PatientTherapyProgram(patient, program, upfrontSessions);
        ptpDAO.save(ptp);
    }

    // ==================== Conversion Helpers ====================

    private PatientDTO toDTO(Patient entity) {
        PatientDTO dto = new PatientDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setRegisteredDate(entity.getRegisteredDate());
        dto.setInterviewNote(entity.getInterviewNote());
        return dto;
    }
    public TherapyProgramDTO toDTO(TherapyProgram entity) {
        TherapyProgramDTO dto = new TherapyProgramDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDuration(entity.getDuration());
        dto.setFee(entity.getFee());
        dto.setTotalSessions(entity.getTotalSessions());
        dto.setSessionFee(entity.getSessionFee());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    private PatientTherapyProgramDTO toPtpDTO(PatientTherapyProgram entity) {
        PatientTherapyProgramDTO dto = new PatientTherapyProgramDTO();
        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        dto.setProgramId(entity.getProgram() != null ? entity.getProgram().getId() : null);
        dto.setUpfrontSessionsPaid(entity.getUpfrontSessionsPaid());
        dto.setSessionsUsed(entity.getSessionsUsed());
        dto.setProgram(entity.getProgram() != null ? toDTO(entity.getProgram()) : null);
        dto.setPatient(entity.getPatient() != null ? toDTO(entity.getPatient()) : null);
        // Display helpers
        if (entity.getProgram() != null) {
            dto.setProgramName(entity.getProgram().getName());
            dto.setTotalSessions(entity.getProgram().getTotalSessions() != null ? entity.getProgram().getTotalSessions() : 0);
        }
        return dto;
    }
}
