package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientTherapyProgramDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PaymentDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapistDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapyProgramDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionPaymentStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SchedulingException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TherapySessionBOImpl implements TherapySessionBO {
    private final TherapySessionDAOImpl sessionDAO = new TherapySessionDAOImpl();
    private final PatientTherapyProgramDAOImpl ptpDAO = new PatientTherapyProgramDAOImpl();
    private final PatientDAOImpl patientDAO = new PatientDAOImpl();
    private final TherapistDAOImpl therapistDAO = new TherapistDAOImpl();
    private final TherapyProgramDAOImpl programDAO = new TherapyProgramDAOImpl();
    private final PaymentDAOImpl paymentDAO = new PaymentDAOImpl();

    public TherapySessionDTO createAndScheduleSession(TherapySessionDTO sessionDTO) throws Exception {
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

        // Check upfront credit
        PatientTherapyProgram ptp = ptpDAO.findByPatientAndProgram(patientId, programId);
        int remainingCredit = (ptp != null) ? ptp.getRemainingCredit() : 0;

        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession therapySession = new TherapySession();
            therapySession.setPatient(patientDAO.getById(patientId, session));
            therapySession.setProgram(programDAO.getById(programId, session));
            if (sessionDTO.getTherapistId() != null) {
                therapySession.setTherapist(therapistDAO.getById(sessionDTO.getTherapistId(), session));
            }
            therapySession.setSessionDate(sessionDTO.getSessionDate());
            therapySession.setSessionTime(sessionDTO.getSessionTime());
            therapySession.setNotes(sessionDTO.getNotes());
            therapySession.setSequenceNumber((int) existingCount + 1);

            if (remainingCredit > 0) {
                // Has credit — create as SCHEDULED + PAID
                if (therapySession.getTherapist() == null) {
                    throw new SchedulingException("Therapist is required to schedule a session.");
                }
                if (therapySession.getSessionDate() == null) {
                    throw new SchedulingException("Session date is required.");
                }

                // Validate date is not in the past
                validateSessionDate(therapySession.getSessionDate());

                // Check therapist availability
                checkTherapistAvailability(therapySession);

                // Find the upfront payment and link it to the session
                Payment upfrontPayment = paymentDAO.findUpfrontByPatient(patientId, session);

                therapySession.setStatus(TherapySession.SessionStatus.SCHEDULED);
                therapySession.setPaymentStatus(TherapySession.PaymentStatus.PAID);
                therapySession.setPayment(upfrontPayment);
                sessionDAO.save(therapySession, session);

                // Deduct credit
                ptpDAO.deductCredit(patientId, programId);
            } else {
                // No credit — create as UNSCHEDULED + PENDING (no date/time)
                therapySession.setSessionDate(null);
                therapySession.setSessionTime(null);
                therapySession.setTherapist(null);
                therapySession.setStatus(TherapySession.SessionStatus.UNSCHEDULED);
                therapySession.setPaymentStatus(TherapySession.PaymentStatus.PENDING);
                sessionDAO.save(therapySession, session);
            }

            transaction.commit();
            return toDTO(therapySession);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

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

        // Validate date is not in the past
        validateSessionDate(session.getSessionDate());

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

        // Validate date is not in the past when scheduling
        if (session.getStatus() == TherapySession.SessionStatus.SCHEDULED && session.getSessionDate() != null) {
            validateSessionDate(session.getSessionDate());
        }

        if (session.getTherapist() != null) {
            checkTherapistAvailability(session);
        }
        sessionDAO.update(session);
    }

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

    private void validateSessionDate(LocalDate sessionDate) {
        if (sessionDate != null && sessionDate.isBefore(LocalDate.now())) {
            throw new SchedulingException("Cannot schedule a session on a past date (" + sessionDate + "). Please select today or a future date.");
        }
    }

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
