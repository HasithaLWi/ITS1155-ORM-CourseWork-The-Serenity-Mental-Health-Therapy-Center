package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PaymentDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentType;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PaymentException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public class PaymentBOImpl implements PaymentBO {
    private final PaymentDAOImpl paymentDAO = new PaymentDAOImpl();
    private final TherapySessionDAOImpl sessionDAO = new TherapySessionDAOImpl();
    private final PatientDAOImpl patientDAO = new PatientDAOImpl();

    public void processPayment(PaymentDTO dto) {
        if (dto.getSessionId() == null) {
            throw new PaymentException("Session is required for payment.");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new PaymentException("Payment amount must be greater than zero.");
        }
        if (dto.getMethod() == null) {
            throw new PaymentException("Payment method is required.");
        }

        TherapySession session = sessionDAO.getById(dto.getSessionId());
        if (session == null) throw new PaymentException("Session not found.");

        Payment payment = new Payment();
        payment.setSession(session);
        payment.setPatient(session.getPatient());
        payment.setAmount(dto.getAmount());
        payment.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentType(Payment.PaymentType.SINGLE);
        payment.setPaymentDate(LocalDateTime.now());

        // Update session payment status
        session.setPaymentStatus(TherapySession.PaymentStatus.PAID);

        paymentDAO.save(payment);
        sessionDAO.update(session);
    }

    /**
     * Process upfront payment for multiple sessions (by session IDs).
     */
    public void processUpfrontPayment(PaymentDTO dto, List<Long> sessionIds) {
        if (dto.getPatientId() == null) {
            throw new PaymentException("Patient is required for upfront payment.");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new PaymentException("Payment amount must be greater than zero.");
        }
        if (dto.getMethod() == null) {
            throw new PaymentException("Payment method is required.");
        }
        if (sessionIds == null || sessionIds.isEmpty()) {
            throw new PaymentException("At least one session must be selected for upfront payment.");
        }

        Patient patient = patientDAO.getById(dto.getPatientId());
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

        paymentDAO.save(payment);

        // Link sessions and bulk update
        for (Long sessionId : sessionIds) {
            TherapySession session = sessionDAO.getById(sessionId);
            if (session != null) {
                session.setUpfrontPayment(payment);
                session.setPaymentStatus(TherapySession.PaymentStatus.PAID);
                sessionDAO.update(session);
            }
        }
    }

    /**
     * Process payment for a single session (inline quick-pay from Session Management).
     * Marks the session as PAID so it can be scheduled.
     */
    public void processSessionPayment(PaymentDTO dto, Long sessionId) {
        TherapySession session = sessionDAO.getById(sessionId);
        if (session == null) {
            throw new PaymentException("Session not found.");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new PaymentException("Payment amount must be greater than zero.");
        }
        if (dto.getMethod() == null) {
            throw new PaymentException("Payment method is required.");
        }

        Payment payment = new Payment();
        payment.setSession(session);
        payment.setPatient(session.getPatient());
        payment.setAmount(dto.getAmount());
        payment.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentType(Payment.PaymentType.SINGLE);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setDescription("Session payment for session #" + session.getId());

        paymentDAO.save(payment);

        session.setPaymentStatus(TherapySession.PaymentStatus.PAID);
        session.setPayment(payment);
        sessionDAO.update(session);
    }

    /**
     * Save a simple upfront payment (used during patient registration).
     */
    public void saveRegistrationPayment(PaymentDTO dto) {
        if (dto.getPatientId() == null) {
            throw new PaymentException("Patient is required.");
        }

        Patient patient = patientDAO.getById(dto.getPatientId());
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

        paymentDAO.save(payment);
    }

    public void updatePayment(PaymentDTO dto) {
        Payment entity = paymentDAO.getById(dto.getId());
        if (entity == null) throw new PaymentException("Payment not found.");
        entity.setAmount(dto.getAmount());
        if (dto.getMethod() != null) entity.setMethod(Payment.PaymentMethod.valueOf(dto.getMethod().name()));
        if (dto.getStatus() != null) entity.setStatus(Payment.PaymentStatus.valueOf(dto.getStatus().name()));
        entity.setDescription(dto.getDescription());
        paymentDAO.update(entity);
    }

    public void deletePayment(Long id) {
        Payment entity = paymentDAO.getById(id);
        if (entity == null) throw new PaymentException("Payment not found.");
        paymentDAO.delete(entity);
    }

    public List<PaymentDTO> getAllPayments() {
        return paymentDAO.getAllWithDetails().stream().map(this::toDTO).toList();
    }

    public PaymentDTO getPaymentBySession(Long sessionId) {
        Payment entity = paymentDAO.findBySession(sessionId);
        return entity != null ? toDTO(entity) : null;
    }

    public BigDecimal getMonthlyRevenue() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        return paymentDAO.getTotalRevenue(start, end);
    }

    public BigDecimal getTotalRevenue() {
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now();
        return paymentDAO.getTotalRevenue(start, end);
    }

    public long getPaymentCount() {
        return paymentDAO.count();
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
        dto.setSessionId(entity.getSession() != null ? entity.getSession().getId() : null);
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        // Display helpers
        dto.setPatientName(entity.getPatient() != null ? entity.getPatient().getName() : 
                (entity.getSession() != null && entity.getSession().getPatient() != null ? entity.getSession().getPatient().getName() : "N/A"));
        return dto;
    }
}
