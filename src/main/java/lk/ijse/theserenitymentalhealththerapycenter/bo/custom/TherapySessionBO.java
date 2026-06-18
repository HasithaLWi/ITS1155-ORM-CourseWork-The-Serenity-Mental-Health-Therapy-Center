package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;


import java.time.LocalDate;
import java.util.List;

public interface TherapySessionBO extends SuperBO {
    TherapySessionDTO createAndScheduleSession(TherapySessionDTO sessionDTO) throws Exception;
    void updateSession(TherapySessionDTO sessionDTO);
    void cancelAndReschedule(Long sessionId);
    long countCompletedByPatientAndProgram(Long patientId, Long programId);
    void deleteSession(Long sessionId);
    TherapySessionDTO getSessionById(Long id);
    List<TherapySessionDTO> getAllSessionDTOs();
    List<TherapySessionDTO> getSessionsByPatient(Long patientId);
    long getSessionCount();
    List<TherapySessionDTO> getScheduledSessionsSortedByDate();
    List<TherapySessionDTO> getTherapySessionsByTherapist(long therapistID);
}
