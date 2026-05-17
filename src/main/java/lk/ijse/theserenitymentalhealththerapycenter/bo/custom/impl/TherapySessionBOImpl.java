package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SchedulingException;

import java.time.LocalDate;
import java.util.List;

public class TherapySessionBOImpl implements TherapySessionBO {
    private final TherapySessionDAOImpl sessionDAO = new TherapySessionDAOImpl();

    public void scheduleSession(TherapySession session) {
        // This is essentially Scenario 3 or 4: Booking a session
        if (session.getPatient() == null) {
            throw new SchedulingException("Patient is required.");
        }
        if (session.getTherapist() == null) {
            throw new SchedulingException("Therapist is required.");
        }
        if (session.getSessionDate() == null) {
            throw new SchedulingException("Session date is required.");
        }
        
        session.setStatus(TherapySession.SessionStatus.SCHEDULED);
        
        // If it's a new unsaved session, save it. But our flow usually updates an existing UNSCHEDULED one.
        if (session.getId() == null) {
            sessionDAO.save(session);
        } else {
            sessionDAO.update(session);
        }
    }

    public void updateSession(TherapySession session) {
        if (session != null) {

            long therapist = session.getTherapist().getId();
            getAllSessions().stream()
                .filter(s -> s.getTherapist() != null && s.getTherapist().getId().equals(therapist))
                .filter(s -> s.getSessionDate() != null && s.getSessionDate().equals(session.getSessionDate()))
                    .filter(s -> s.getSessionTime() != null && s.getSessionTime().equals(session.getSessionTime()))
                .filter(s -> !s.getId().equals(session.getId())) // Exclude current session
                .findAny()
                .ifPresent(s -> {
                    throw new SchedulingException("Therapist is already booked for this date.");
                });

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
     * Cancel a scheduled session (Scenario 6). Reverts to UNSCHEDULED.
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
}
