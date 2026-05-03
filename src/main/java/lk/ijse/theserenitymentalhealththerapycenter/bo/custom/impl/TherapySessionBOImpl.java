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
        if (session.getPatient() == null) {
            throw new SchedulingException("Patient is required.");
        }
        if (session.getTherapist() == null) {
            throw new SchedulingException("Therapist is required.");
        }
        if (session.getSessionDate() == null) {
            throw new SchedulingException("Session date is required.");
        }
        sessionDAO.save(session);
    }

    public void updateSession(TherapySession session) {
        sessionDAO.update(session);
    }

    public void cancelSession(TherapySession session) {
        session.setStatus(TherapySession.SessionStatus.CANCELLED);
        sessionDAO.update(session);
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
