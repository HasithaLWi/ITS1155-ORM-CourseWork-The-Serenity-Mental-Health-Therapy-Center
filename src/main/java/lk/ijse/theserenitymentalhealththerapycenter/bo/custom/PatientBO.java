package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientTherapyProgramDTO;

import java.util.List;

public interface PatientBO extends SuperBO {
    Long registerPatient(PatientDTO patient);
    void updatePatient(PatientDTO dto);
    void deletePatient(Long id);
    PatientDTO getPatientById(Long id);
    List<PatientDTO> getAllPatients();
    List<PatientDTO> searchPatients(String name);
    List<PatientDTO> getAllWithPrograms();
    List<PatientDTO> findPatientsInAllPrograms();
    long getPatientCount();
    List<PatientTherapyProgramDTO> getPatientPrograms(Long patientId);
    PatientTherapyProgramDTO getPatientProgram(Long patientId, Long programId);
    void deductUpfrontCredit(Long patientId, Long programId);
    void enrollPatientInProgram(Long patientId, Long programId, int upfrontSessions);
}
