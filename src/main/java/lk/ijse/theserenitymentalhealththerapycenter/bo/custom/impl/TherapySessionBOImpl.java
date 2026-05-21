package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.*;
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
    private final TherapySessionDAO sessionDAO =
            (TherapySessionDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPY_SESSION);
    private final PatientTherapyProgramDAO ptpDAO =
            (PatientTherapyProgramDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PATIENT_THERAPY_PROGRAM);
    private final PatientDAO patientDAO =
            (PatientDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PATIENT);
    private final TherapistDAO therapistDAO =
            (TherapistDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPIST);
    private final TherapyProgramDAO programDAO =
            (TherapyProgramDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPY_PROGRAM);
    private final PaymentDAO paymentDAO =
            (PaymentDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PAYMENT);

    public TherapySessionDTO createAndScheduleSession(TherapySessionDTO sessionDTO) {
        if (sessionDTO.getPatientId() == null) throw new SchedulingException("Patient is required.");
        if (sessionDTO.getProgramId() == null) throw new SchedulingException("Program is required.");

        Long patientId = sessionDTO.getPatientId();
        Long programId = sessionDTO.getProgramId();

        long existingCount = sessionDAO.countByPatientAndProgram(patientId, programId);
        Integer totalSessions = programDAO.getById(programId).getTotalSessions();
        if (totalSessions != null && totalSessions > 0 && existingCount >= totalSessions) {
            throw new SchedulingException("Maximum sessions (" + totalSessions + ") already reached for this program.");
        }

        PatientTherapyProgram ptp = ptpDAO.findByPatientAndProgram(patientId, programId);
        int remainingCredit = (ptp != null) ? ptp.getRemainingCredit() : 0;

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = new TherapySession();
            ts.setPatient(patientDAO.getById(patientId, session));
            ts.setProgram(programDAO.getById(programId, session));
            if (sessionDTO.getTherapistId() != null) ts.setTherapist(therapistDAO.getById(sessionDTO.getTherapistId(), session));
            ts.setSessionDate(sessionDTO.getSessionDate());
            ts.setSessionTime(sessionDTO.getSessionTime());
            ts.setNotes(sessionDTO.getNotes());
            ts.setSequenceNumber((int) existingCount + 1);

            if (remainingCredit > 0) {
                if (ts.getTherapist() == null) throw new SchedulingException("Therapist is required to schedule a session.");
                if (ts.getSessionDate() == null) throw new SchedulingException("Session date is required.");
                validateSessionDate(ts.getSessionDate());
                checkTherapistAvailability(ts);

                Payment upfrontPayment = paymentDAO.findUpfrontByPatient(patientId, session);
                ts.setStatus(TherapySession.SessionStatus.SCHEDULED);
                ts.setPaymentStatus(TherapySession.PaymentStatus.PAID);
                ts.setPayment(upfrontPayment);
                sessionDAO.save(ts, session);
                ptpDAO.deductCredit(patientId, programId, session);
            } else {
                ts.setSessionDate(null);
                ts.setSessionTime(null);
                ts.setTherapist(null);
                ts.setStatus(TherapySession.SessionStatus.UNSCHEDULED);
                ts.setPaymentStatus(TherapySession.PaymentStatus.PENDING);
                sessionDAO.save(ts, session);
            }

            transaction.commit();
            return toDTO(ts);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void scheduleSession(TherapySessionDTO sessionDTO) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = sessionDAO.getById(sessionDTO.getId(), session);
            if (ts == null) throw new SchedulingException("Session not found.");
            if (ts.getPatient() == null) throw new SchedulingException("Patient is required.");

            if (sessionDTO.getTherapistId() != null) ts.setTherapist(therapistDAO.getById(sessionDTO.getTherapistId(), session));
            if (ts.getTherapist() == null) throw new SchedulingException("Therapist is required.");

            ts.setSessionDate(sessionDTO.getSessionDate());
            ts.setSessionTime(sessionDTO.getSessionTime());
            if (ts.getSessionDate() == null) throw new SchedulingException("Session date is required.");

            validateSessionDate(ts.getSessionDate());

            if (ts.getPaymentStatus() == TherapySession.PaymentStatus.PENDING)
                throw new SchedulingException("Payment is still PENDING for this session. Please pay first.");

            checkTherapistAvailability(ts);
            ts.setStatus(TherapySession.SessionStatus.SCHEDULED);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void updateSession(TherapySessionDTO sessionDTO) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = sessionDAO.getById(sessionDTO.getId(), session);
            if (ts == null) throw new SchedulingException("Session not found.");

            if (sessionDTO.getPatientId() != null) ts.setPatient(patientDAO.getById(sessionDTO.getPatientId(), session));
            if (sessionDTO.getTherapistId() != null) ts.setTherapist(therapistDAO.getById(sessionDTO.getTherapistId(), session));
            if (sessionDTO.getProgramId() != null) ts.setProgram(programDAO.getById(sessionDTO.getProgramId(), session));

            ts.setSessionDate(sessionDTO.getSessionDate());
            ts.setSessionTime(sessionDTO.getSessionTime());
            if (sessionDTO.getStatus() != null) ts.setStatus(TherapySession.SessionStatus.valueOf(sessionDTO.getStatus().name()));
            ts.setNotes(sessionDTO.getNotes());

            if (ts.getStatus() == TherapySession.SessionStatus.SCHEDULED && ts.getSessionDate() != null) {
                validateSessionDate(ts.getSessionDate());
            }
            if (ts.getTherapist() != null) checkTherapistAvailability(ts);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public TherapySessionDTO completeSession(Long sessionId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = sessionDAO.getById(sessionId, session);
            if (ts == null) throw new SchedulingException("Session not found.");
            ts.setStatus(TherapySession.SessionStatus.COMPLETED);
            transaction.commit();

            List<TherapySession> unscheduled = sessionDAO.findUnscheduledByPatient(ts.getPatient().getId());
            if (unscheduled != null && !unscheduled.isEmpty()) return toDTO(unscheduled.get(0));
            return null;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void cancelAndReschedule(Long sessionId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = sessionDAO.getById(sessionId, session);
            if (ts == null) throw new SchedulingException("Session not found.");
            ts.setStatus(TherapySession.SessionStatus.UNSCHEDULED);
            ts.setSessionDate(null);
            ts.setSessionTime(null);
            ts.setTherapist(null);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void cancelSession(Long sessionId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = sessionDAO.getById(sessionId, session);
            if (ts == null) throw new SchedulingException("Session not found.");
            ts.setStatus(TherapySession.SessionStatus.CANCELLED);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
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
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = sessionDAO.getById(sessionId, session);
            if (ts == null) throw new SchedulingException("Session not found.");
            sessionDAO.delete(ts, session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public TherapySessionDTO getSessionById(Long id) {
        TherapySession ts = sessionDAO.getById(id);
        return ts != null ? toDTO(ts) : null;
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

    public long getTodaySessionCount() { return sessionDAO.countByDate(LocalDate.now()); }
    public long getSessionCount() { return sessionDAO.count(); }

    // ==================== Validation Helpers ====================

    private void validateSessionDate(LocalDate sessionDate) {
        if (sessionDate != null && sessionDate.isBefore(LocalDate.now())) {
            throw new SchedulingException("Cannot schedule a session on a past date (" + sessionDate + "). Please select today or a future date.");
        }
    }

    private void checkTherapistAvailability(TherapySession ts) {
        if (ts.getTherapist() == null || ts.getSessionDate() == null || ts.getSessionTime() == null) return;
        long therapistId = ts.getTherapist().getId();
        sessionDAO.getAllWithDetails().stream()
                .filter(s -> s.getTherapist() != null && s.getTherapist().getId().equals(therapistId))
                .filter(s -> s.getSessionDate() != null && s.getSessionDate().equals(ts.getSessionDate()))
                .filter(s -> s.getSessionTime() != null && s.getSessionTime().equals(ts.getSessionTime()))
                .filter(s -> ts.getId() == null || !s.getId().equals(ts.getId()))
                .findAny()
                .ifPresent(s -> { throw new SchedulingException("Therapist is already booked for this date and time."); });
    }

    // ==================== Conversion ====================

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
        dto.setPatientName(entity.getPatient() != null ? entity.getPatient().getName() : null);
        dto.setTherapistName(entity.getTherapist() != null ? entity.getTherapist().getName() : null);
        dto.setProgramName(entity.getProgram() != null ? entity.getProgram().getName() : null);
        return dto;
    }
}
