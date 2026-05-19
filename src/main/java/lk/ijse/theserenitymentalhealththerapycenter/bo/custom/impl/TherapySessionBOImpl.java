package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientTherapyProgramDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SchedulingException;

import java.time.LocalDate;
import java.util.List;

public class TherapySessionBOImpl implements TherapySessionBO {
    private final TherapySessionDAOImpl sessionDAO = new TherapySessionDAOImpl();
    private final PatientTherapyProgramDAOImpl ptpDAO = new PatientTherapyProgramDAOImpl();

    /**
     * Create and schedule a new session on-demand.
     * If upfront credit is available, the session is created as SCHEDULED + PAID.
     * If no credit, the session is created as UNSCHEDULED + PENDING (no date/time).
     *
     * @return the created session
     */
    public TherapySession createAndScheduleSession(TherapySession session) {
        if (session.getPatient() == null) {
            throw new SchedulingException("Patient is required.");
        }
        if (session.getProgram() == null) {
            throw new SchedulingException("Program is required.");
        }

        Long patientId = session.getPatient().getId();
        Long programId = session.getProgram().getId();

        // Auto-assign sequence number
        long existingCount = sessionDAO.countByPatientAndProgram(patientId, programId);
        
        Integer totalSessions = session.getProgram().getTotalSessions();
        if (totalSessions != null && totalSessions > 0 && existingCount >= totalSessions) {
            throw new SchedulingException("Maximum sessions (" + totalSessions + ") already reached for this program.");
        }
        
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

        return session;
    }

    /**
     * Schedule an existing UNSCHEDULED session that has been paid for.
     */
    public void scheduleSession(TherapySession session) {
        if (session.getPatient() == null) {
            throw new SchedulingException("Patient is required.");
        }
        if (session.getTherapist() == null) {
            throw new SchedulingException("Therapist is required.");
        }
        if (session.getSessionDate() == null) {
            throw new SchedulingException("Session date is required.");
        }
        if (session.getPaymentStatus() == TherapySession.PaymentStatus.PENDING) {
            throw new SchedulingException("Payment is still PENDING for this session. Please pay first.");
        }

        // Check therapist availability
        checkTherapistAvailability(session);

        session.setStatus(TherapySession.SessionStatus.SCHEDULED);
        
        if (session.getId() == null) {
            sessionDAO.save(session);
        } else {
            sessionDAO.update(session);
        }
    }

    public void updateSession(TherapySession session) {
        if (session != null && session.getTherapist() != null) {
            checkTherapistAvailability(session);
        }
        sessionDAO.update(session);
    }

    /**
     * Complete a session and potentially return the next unscheduled session.
     * Returns null if no more sessions in the program.
     */
    public TherapySession completeSession(TherapySession session) {
        session.setStatus(TherapySession.SessionStatus.COMPLETED);
        sessionDAO.update(session);
        
        // Find next unscheduled session for this patient/program
        List<TherapySession> unscheduled = sessionDAO.findUnscheduledByPatient(session.getPatient().getId());
        if (unscheduled != null && !unscheduled.isEmpty()) {
            return unscheduled.get(0);
        }
        return null;
    }

    /**
     * Cancel a scheduled session (Scenario 6). Reverts to UNSCHEDULED with no date/time.
     */
    public void cancelAndReschedule(TherapySession session) {
        session.setStatus(TherapySession.SessionStatus.UNSCHEDULED);
        session.setSessionDate(null);
        session.setSessionTime(null);
        session.setTherapist(null);
        sessionDAO.update(session);
    }
    
    public void cancelSession(TherapySession session) {
        session.setStatus(TherapySession.SessionStatus.CANCELLED);
        sessionDAO.update(session);
    }

    public long countCompletedByPatientAndProgram(Long patientId, Long programId) {
        return sessionDAO.countCompletedByPatientAndProgram(patientId, programId);
    }

    public long countByPatientAndProgram(Long patientId, Long programId) {
        return sessionDAO.countByPatientAndProgram(patientId, programId);
    }

    public List<TherapySession> findUnscheduledByPatient(Long patientId) {
        return sessionDAO.findUnscheduledByPatient(patientId);
    }

    public void deleteSession(TherapySession session) {
        sessionDAO.delete(session);
    }

    public TherapySession getSessionById(Long id) {
        return sessionDAO.getById(id);
    }

    public List<TherapySession> getAllSessions() {
        return sessionDAO.getAllWithDetails();
    }

    public List<TherapySession> getTodaySessions() {
        return sessionDAO.findByDate(LocalDate.now());
    }

    public List<TherapySession> getSessionsByDate(LocalDate date) {
        return sessionDAO.findByDate(date);
    }

    public List<TherapySession> getSessionsByPatient(Long patientId) {
        return sessionDAO.findByPatient(patientId);
    }

    public List<TherapySession> getSessionsByTherapist(Long therapistId) {
        return sessionDAO.findByTherapist(therapistId);
    }

    public List<TherapySession> getSessionsByDateRange(LocalDate start, LocalDate end) {
        return sessionDAO.findByDateRange(start, end);
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
        getAllSessions().stream()
                .filter(s -> s.getTherapist() != null && s.getTherapist().getId().equals(therapistId))
                .filter(s -> s.getSessionDate() != null && s.getSessionDate().equals(session.getSessionDate()))
                .filter(s -> s.getSessionTime() != null && s.getSessionTime().equals(session.getSessionTime()))
                .filter(s -> session.getId() == null || !s.getId().equals(session.getId()))
                .findAny()
                .ifPresent(s -> {
                    throw new SchedulingException("Therapist is already booked for this date and time.");
                });
    }
}
