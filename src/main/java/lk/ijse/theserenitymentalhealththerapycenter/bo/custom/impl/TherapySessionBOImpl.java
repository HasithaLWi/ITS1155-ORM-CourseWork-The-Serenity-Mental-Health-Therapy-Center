package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientTherapyProgramDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapistDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapyProgramDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionPaymentStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SchedulingException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TherapySessionBOImpl implements TherapySessionBO {
    private final TherapySessionDAOImpl sessionDAO = new TherapySessionDAOImpl();
    private final PatientTherapyProgramDAOImpl ptpDAO = new PatientTherapyProgramDAOImpl();
    private final PatientDAOImpl patientDAO = new PatientDAOImpl();
    private final TherapistDAOImpl therapistDAO = new TherapistDAOImpl();
    private final TherapyProgramDAOImpl programDAO = new TherapyProgramDAOImpl();

    /**
     * Create and schedule a new session on-demand.
     * If upfront credit is available, the session is created as SCHEDULED + PAID.
     * If no credit, the session is created as UNSCHEDULED + PENDING (no date/time).
     *
     * @return the created session DTO
     */
    public TherapySessionDTO createAndScheduleSession(TherapySessionDTO sessionDTO) {
        if (sessionDTO.getPatientId() == null) {
            throw new SchedulingException("Patient is required.");
        }
        if (sessionDTO.getProgramId() == null) {
            throw new SchedulingException("Program is required.");
        }

        Long patientId = sessionDTO.getPatientId();
        Long programId = sessionDTO.getProgramId();

        // Auto-assign sequence number
        long existingCount = sessionDAO.countByPatientAndProgram(patientId, programId);
        
        Integer totalSessions = programDAO.getById(programId).getTotalSessions();
        if (totalSessions != null && totalSessions > 0 && existingCount >= totalSessions) {
            throw new SchedulingException("Maximum sessions (" + totalSessions + ") already reached for this program.");
        }
        
        TherapySession session = new TherapySession();
        session.setPatient(patientDAO.getById(patientId));
        session.setProgram(programDAO.getById(programId));
        if (sessionDTO.getTherapistId() != null) {
            session.setTherapist(therapistDAO.getById(sessionDTO.getTherapistId()));
        }
        session.setSessionDate(sessionDTO.getSessionDate());
        session.setSessionTime(sessionDTO.getSessionTime());
        session.setNotes(sessionDTO.getNotes());
        session.setSequenceNumber((int) existingCount + 1);

        // Check upfront credit
        PatientTherapyProgram ptp = ptpDAO.findByPatientAndProgram(patientId, programId);
        int remainingCredit = (ptp != null) ? ptp.getRemainingCredit() : 0;

        if (remainingCredit > 0) {
            // Has credit — create as SCHEDULED + PAID
            if (session.getTherapist() == null) {
                throw new SchedulingException("Therapist is required to schedule a session.");
            }
            if (session.getSessionDate() == null) {
                throw new SchedulingException("Session date is required.");
            }

            // Check therapist availability
            checkTherapistAvailability(session);

            session.setStatus(TherapySession.SessionStatus.SCHEDULED);
            session.setPaymentStatus(TherapySession.PaymentStatus.PAID);
            sessionDAO.save(session);

            // Deduct credit
            ptpDAO.deductCredit(patientId, programId);
        } else {
            // No credit — create as UNSCHEDULED + PENDING (no date/time)
            session.setSessionDate(null);
            session.setSessionTime(null);
            session.setTherapist(null);
            session.setStatus(TherapySession.SessionStatus.UNSCHEDULED);
            session.setPaymentStatus(TherapySession.PaymentStatus.PENDING);
            sessionDAO.save(session);
        }

        return toDTO(session);
    }

    /**
     * Schedule an existing UNSCHEDULED session that has been paid for.
     */
    public void scheduleSession(TherapySessionDTO sessionDTO) {
        TherapySession session = sessionDAO.getById(sessionDTO.getId());
        if (session == null) throw new SchedulingException("Session not found.");
        
        if (session.getPatient() == null) {
            throw new SchedulingException("Patient is required.");
        }
        
        session.setTherapist(sessionDTO.getTherapistId() != null ? therapistDAO.getById(sessionDTO.getTherapistId()) : null);
        
        if (session.getTherapist() == null) {
            throw new SchedulingException("Therapist is required.");
        }
        
        session.setSessionDate(sessionDTO.getSessionDate());
        session.setSessionTime(sessionDTO.getSessionTime());
        
        if (session.getSessionDate() == null) {
            throw new SchedulingException("Session date is required.");
        }
        if (session.getPaymentStatus() == TherapySession.PaymentStatus.PENDING) {
            throw new SchedulingException("Payment is still PENDING for this session. Please pay first.");
        }

        // Check therapist availability
        checkTherapistAvailability(session);

        session.setStatus(TherapySession.SessionStatus.SCHEDULED);
        sessionDAO.update(session);
    }

    public void updateSession(TherapySessionDTO sessionDTO) {
        TherapySession session = sessionDAO.getById(sessionDTO.getId());
        if (session == null) throw new SchedulingException("Session not found.");
        
        session.setPatient(sessionDTO.getPatientId() != null ? patientDAO.getById(sessionDTO.getPatientId()) : null);
        session.setTherapist(sessionDTO.getTherapistId() != null ? therapistDAO.getById(sessionDTO.getTherapistId()) : null);
        session.setProgram(sessionDTO.getProgramId() != null ? programDAO.getById(sessionDTO.getProgramId()) : null);
        
        session.setSessionDate(sessionDTO.getSessionDate());
        session.setSessionTime(sessionDTO.getSessionTime());
        if (sessionDTO.getStatus() != null) session.setStatus(TherapySession.SessionStatus.valueOf(sessionDTO.getStatus().name()));
        session.setNotes(sessionDTO.getNotes());
        
        if (session.getTherapist() != null) {
            checkTherapistAvailability(session);
        }
        sessionDAO.update(session);
    }

    /**
     * Complete a session and potentially return the next unscheduled session.
     * Returns null if no more sessions in the program.
     */
    public TherapySessionDTO completeSession(Long sessionId) {
        TherapySession session = sessionDAO.getById(sessionId);
        if (session == null) throw new SchedulingException("Session not found.");
        
        session.setStatus(TherapySession.SessionStatus.COMPLETED);
        sessionDAO.update(session);
        
        // Find next unscheduled session for this patient/program
        List<TherapySession> unscheduled = sessionDAO.findUnscheduledByPatient(session.getPatient().getId());
        if (unscheduled != null && !unscheduled.isEmpty()) {
            return toDTO(unscheduled.get(0));
        }
        return null;
    }

    /**
     * Cancel a scheduled session (Scenario 6). Reverts to UNSCHEDULED with no date/time.
     */
    public void cancelAndReschedule(Long sessionId) {
        TherapySession session = sessionDAO.getById(sessionId);
        if (session == null) throw new SchedulingException("Session not found.");
        
        session.setStatus(TherapySession.SessionStatus.UNSCHEDULED);
        session.setSessionDate(null);
        session.setSessionTime(null);
        session.setTherapist(null);
        sessionDAO.update(session);
    }
    
    public void cancelSession(Long sessionId) {
        TherapySession session = sessionDAO.getById(sessionId);
        if (session == null) throw new SchedulingException("Session not found.");
        
        session.setStatus(TherapySession.SessionStatus.CANCELLED);
        sessionDAO.update(session);
    }

    public long countCompletedByPatientAndProgram(Long patientId, Long programId) {
        return sessionDAO.countCompletedByPatientAndProgram(patientId, programId);
    }

    public long countByPatientAndProgram(Long patientId, Long programId) {
        return sessionDAO.countByPatientAndProgram(patientId, programId);
    }

    public List<TherapySessionDTO> findUnscheduledByPatient(Long patientId) {
        return sessionDAO.findUnscheduledByPatient(patientId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void deleteSession(Long sessionId) {
        TherapySession session = sessionDAO.getById(sessionId);
        if (session == null) throw new SchedulingException("Session not found.");
        sessionDAO.delete(session);
    }

    public TherapySessionDTO getSessionById(Long id) {
        TherapySession session = sessionDAO.getById(id);
        return session != null ? toDTO(session) : null;
    }

    /**
     * Get all sessions as DTOs (for use in controllers that must not touch entities).
     */
    public List<TherapySessionDTO> getAllSessionDTOs() {
        return sessionDAO.getAllWithDetails().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TherapySessionDTO> getTodaySessions() {
        return sessionDAO.findByDate(LocalDate.now()).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TherapySessionDTO> getSessionsByDate(LocalDate date) {
        return sessionDAO.findByDate(date).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TherapySessionDTO> getSessionsByPatient(Long patientId) {
        return sessionDAO.findByPatient(patientId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TherapySessionDTO> getSessionsByTherapist(Long therapistId) {
        return sessionDAO.findByTherapist(therapistId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TherapySessionDTO> getSessionsByDateRange(LocalDate start, LocalDate end) {
        return sessionDAO.findByDateRange(start, end).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public long getTodaySessionCount() {
        return sessionDAO.countByDate(LocalDate.now());
    }

    public long getSessionCount() {
        return sessionDAO.count();
    }

    /**
     * Check if a therapist is already booked at the same date/time (excluding the current session).
     */
    private void checkTherapistAvailability(TherapySession session) {
        if (session.getTherapist() == null || session.getSessionDate() == null || session.getSessionTime() == null) {
            return;
        }

        long therapistId = session.getTherapist().getId();
        sessionDAO.getAllWithDetails().stream()
                .filter(s -> s.getTherapist() != null && s.getTherapist().getId().equals(therapistId))
                .filter(s -> s.getSessionDate() != null && s.getSessionDate().equals(session.getSessionDate()))
                .filter(s -> s.getSessionTime() != null && s.getSessionTime().equals(session.getSessionTime()))
                .filter(s -> session.getId() == null || !s.getId().equals(session.getId()))
                .findAny()
                .ifPresent(s -> {
                    throw new SchedulingException("Therapist is already booked for this date and time.");
                });
    }

    // ==================== Conversion Helpers ====================

    public TherapySessionDTO toDTO(TherapySession entity) {
        TherapySessionDTO dto = new TherapySessionDTO();
        dto.setId(entity.getId());
        dto.setSequenceNumber(entity.getSequenceNumber());
        dto.setSessionDate(entity.getSessionDate());
        dto.setSessionTime(entity.getSessionTime());
        dto.setStatus(entity.getStatus() != null ? SessionStatus.valueOf(entity.getStatus().name()) : null);
        dto.setPaymentStatus(entity.getPaymentStatus() != null ? SessionPaymentStatus.valueOf(entity.getPaymentStatus().name()) : null);
        dto.setNotes(entity.getNotes());
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        dto.setTherapistId(entity.getTherapist() != null ? entity.getTherapist().getId() : null);
        dto.setProgramId(entity.getProgram() != null ? entity.getProgram().getId() : null);
        // Display helpers
        dto.setPatientName(entity.getPatient() != null ? entity.getPatient().getName() : null);
        dto.setTherapistName(entity.getTherapist() != null ? entity.getTherapist().getName() : null);
        dto.setProgramName(entity.getProgram() != null ? entity.getProgram().getName() : null);
        return dto;
    }
}
