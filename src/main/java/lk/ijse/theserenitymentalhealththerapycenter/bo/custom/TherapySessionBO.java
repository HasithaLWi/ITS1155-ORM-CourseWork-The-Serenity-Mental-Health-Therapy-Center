package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;


import java.time.LocalDate;
import java.util.List;

public interface TherapySessionBO extends SuperBO {
    TherapySessionDTO createAndScheduleSession(TherapySessionDTO sessionDTO) throws Exception;

    void scheduleSession(TherapySessionDTO sessionDTO);

    void updateSession(TherapySessionDTO sessionDTO);

    TherapySessionDTO completeSession(Long sessionId);


    void cancelAndReschedule(Long sessionId);

    void cancelSession(Long sessionId);

    long countCompletedByPatientAndProgram(Long patientId, Long programId);

    long countByPatientAndProgram(Long patientId, Long programId);

    List<TherapySessionDTO> findUnscheduledByPatient(Long patientId);

    void deleteSession(Long sessionId);

    TherapySessionDTO getSessionById(Long id);

    List<TherapySessionDTO> getAllSessionDTOs();

    List<TherapySessionDTO> getTodaySessions();

    List<TherapySessionDTO> getSessionsByDate(LocalDate date);

    List<TherapySessionDTO> getSessionsByPatient(Long patientId);

    List<TherapySessionDTO> getSessionsByTherapist(Long therapistId);

    List<TherapySessionDTO> getSessionsByDateRange(LocalDate start, LocalDate end);

    long getTodaySessionCount();

    long getSessionCount();


}
