package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PaymentDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.*;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PaymentException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public class PaymentBOImpl implements PaymentBO {
    private final PaymentDAO paymentDAO =
            (PaymentDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PAYMENT);
    private final TherapySessionDAO sessionDAO =
            (TherapySessionDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPY_SESSION);
    private final PatientDAO patientDAO =
            (PatientDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PATIENT);

    public void processPayment(PaymentDTO dto) {
        if (dto.getSessionId() == null) throw new PaymentException("Session is required for payment.");
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) throw new PaymentException("Payment amount must be greater than zero.");
        if (dto.getMethod() == null) throw new PaymentException("Payment method is required.");

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = sessionDAO.getById(dto.getSessionId(), session);
            if (ts == null) throw new PaymentException("Session not found.");

            Payment payment = new Payment();
            payment.setPatient(ts.getPatient());
            payment.setAmount(dto.getAmount());
            payment.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentType(Payment.PaymentType.SINGLE);
            payment.setPaymentDate(LocalDateTime.now());
            paymentDAO.save(payment, session);

            ts.setPayment(payment);
            ts.setPaymentStatus(TherapySession.PaymentStatus.PAID);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void processUpfrontPayment(PaymentDTO dto, List<Long> sessionIds) {
        if (dto.getPatientId() == null) throw new PaymentException("Patient is required for upfront payment.");
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) throw new PaymentException("Payment amount must be greater than zero.");
        if (dto.getMethod() == null) throw new PaymentException("Payment method is required.");
        if (sessionIds == null || sessionIds.isEmpty()) throw new PaymentException("At least one session must be selected for upfront payment.");

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient patient = patientDAO.getById(dto.getPatientId(), session);
            if (patient == null) throw new PaymentException("Patient not found.");

            Payment payment = new Payment();
            payment.setPatient(patient);
            payment.setAmount(dto.getAmount());
            payment.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
            payment.setDiscount(dto.getDiscount());
            payment.setPaymentType(Payment.PaymentType.UPFRONT);
            payment.setDescription(dto.getDescription());
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());
            paymentDAO.save(payment, session);

            for (Long sessionId : sessionIds) {
                TherapySession ts = sessionDAO.getById(sessionId, session);
                if (ts != null) {
                    ts.setPayment(payment);
                    ts.setPaymentStatus(TherapySession.PaymentStatus.PAID);
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void processSessionPayment(PaymentDTO dto, Long sessionId) {
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) throw new PaymentException("Payment amount must be greater than zero.");
        if (dto.getMethod() == null) throw new PaymentException("Payment method is required.");

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TherapySession ts = sessionDAO.getById(sessionId, session);
            if (ts == null) throw new PaymentException("Session not found.");

            Payment payment = new Payment();
            payment.setPatient(ts.getPatient());
            payment.setAmount(dto.getAmount());
            payment.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentType(Payment.PaymentType.SINGLE);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setDescription("Session payment for session #" + ts.getId());
            paymentDAO.save(payment, session);

            ts.setPayment(payment);
            ts.setPaymentStatus(TherapySession.PaymentStatus.PAID);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void saveRegistrationPayment(PaymentDTO dto) {
        if (dto.getPatientId() == null) throw new PaymentException("Patient is required.");

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient patient = patientDAO.getById(dto.getPatientId(), session);
            if (patient == null) throw new PaymentException("Patient not found.");

            Payment payment = new Payment();
            payment.setPatient(patient);
            payment.setAmount(dto.getAmount());
            payment.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
            payment.setDiscount(dto.getDiscount());
            payment.setPaymentType(Payment.PaymentType.UPFRONT);
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setDescription(dto.getDescription());
            paymentDAO.save(payment, session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void updatePayment(PaymentDTO dto) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            Payment entity = paymentDAO.getById(dto.getId(), session);
            if (entity == null) throw new PaymentException("Payment not found.");
            entity.setAmount(dto.getAmount());
            if (dto.getMethod() != null) entity.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
            if (dto.getStatus() != null) entity.setStatus(Payment.PaymentStatus.valueOf(dto.getStatus().name()));
            entity.setDescription(dto.getDescription());
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void deletePayment(Long id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            Payment entity = paymentDAO.getById(id, session);
            if (entity == null) throw new PaymentException("Payment not found.");
            paymentDAO.delete(entity, session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public List<PaymentDTO> getAllPayments() {
        return paymentDAO.getAllWithDetails().stream().map(this::toDTO).toList();
    }

    public PaymentDTO getPaymentBySession(Long sessionId) {
        Payment entity = paymentDAO.findBySession(sessionId);
        return entity != null ? toDTO(entity) : null;
    }

    public BigDecimal getMonthlyRevenue() {
        YearMonth cm = YearMonth.now();
        return paymentDAO.getTotalRevenue(cm.atDay(1).atStartOfDay(), cm.atEndOfMonth().atTime(23, 59, 59));
    }

    public BigDecimal getTotalRevenue() {
        return paymentDAO.getTotalRevenue(LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now());
    }

    public long getPaymentCount() { return paymentDAO.count(); }

    public void processExpense(PaymentDTO dto) {
        if (dto.getPatientId() == null) throw new PaymentException("Patient is required for expense.");
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) throw new PaymentException("Amount must be greater than zero.");
        if (dto.getMethod() == null) throw new PaymentException("Payment method is required.");

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient patient = patientDAO.getById(dto.getPatientId(), session);
            if (patient == null) throw new PaymentException("Patient not found.");

            Payment payment = new Payment();
            payment.setPatient(patient);
            payment.setAmount(dto.getAmount());
            payment.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.setPaymentType(Payment.PaymentType.EXPENSE);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setDescription(dto.getDescription());
            paymentDAO.save(payment, session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public List<PaymentDTO> getFilteredPayments(Long patientId, LocalDateTime start, LocalDateTime end, String paymentType) {
        Payment.PaymentType type = null;
        if (paymentType != null && !paymentType.isEmpty() && !paymentType.equals("ALL"))
            type = Payment.PaymentType.valueOf(paymentType);
        return paymentDAO.findFiltered(patientId, start, end, type).stream().map(this::toDTO).toList();
    }

    public List<PaymentDTO> getPaymentsByPatient(Long patientId) {
        return paymentDAO.findByPatient(patientId).stream().map(this::toDTO).toList();
    }

    // ==================== Conversion Helpers ====================

    private PaymentDTO toDTO(Payment entity) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(entity.getId());
        dto.setAmount(entity.getAmount());
        dto.setPaymentDate(entity.getPaymentDate());
        dto.setMethod(entity.getMethod() != null ? PaymentMethod.valueOf(entity.getMethod().name()) : null);
        dto.setStatus(entity.getStatus() != null ? PaymentStatus.valueOf(entity.getStatus().name()) : null);
        dto.setPaymentType(entity.getPaymentType() != null ? PaymentType.valueOf(entity.getPaymentType().name()) : null);
        dto.setDiscount(entity.getDiscount());
        dto.setDescription(entity.getDescription());
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        dto.setPatientName(entity.getPatient() != null ? entity.getPatient().getName() : "N/A");
        dto.setPatient(toDTO(entity.getPatient()));
        if (entity.getCoveredSessions() != null && !entity.getCoveredSessions().isEmpty())
            dto.setSessionId(entity.getCoveredSessions().get(0).getId());
        return dto;
    }

    private PatientDTO toDTO(Patient entity) {
        PatientDTO dto = new PatientDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setRegisteredDate(entity.getRegisteredDate());
        dto.setInterviewNote(entity.getInterviewNote());
        return dto;
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
        dto.setPatientName(entity.getPatient() != null ? entity.getPatient().getName() : null);
        dto.setTherapistName(entity.getTherapist() != null ? entity.getTherapist().getName() : null);
        dto.setProgramName(entity.getProgram() != null ? entity.getProgram().getName() : null);
        return dto;
    }

    public Patient toEntity(PatientDTO dto) {
        Patient entity = new Patient();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setRegisteredDate(dto.getRegisteredDate());
        entity.setInterviewNote(dto.getInterviewNote());
        return entity;
    }
}
