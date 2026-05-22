package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientTherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDeleteSummaryDTO;

import java.util.List;

public interface PatientBO extends SuperBO {
    Long registerPatient(PatientDTO patient);
    void updatePatient(PatientDTO dto);
    void deletePatient(Long id);
    PatientDTO getPatientById(Long id);
    List<PatientDTO> getAllPatients();
    long getPatientCount();
    List<PatientTherapyProgramDTO> getPatientPrograms(Long patientId);
    PatientTherapyProgramDTO getPatientProgram(Long patientId, Long programId);
    void enrollPatientInProgram(Long patientId, Long programId, int upfrontSessions);
    PatientDeleteSummaryDTO getPatientDeleteSummary(Long patientId);
    List<PatientDTO> getPatientsWithNoScheduledSessions();
}
