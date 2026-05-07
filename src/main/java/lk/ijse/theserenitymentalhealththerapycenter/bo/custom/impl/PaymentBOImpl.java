package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PaymentDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PaymentException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public class PaymentBOImpl implements PaymentBO {
    private final PaymentDAOImpl paymentDAO = new PaymentDAOImpl();

    private final lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl sessionDAO = new lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.TherapySessionDAOImpl();

    public void processPayment(Payment payment) {
        if (payment.getSession() == null) {
            throw new PaymentException("Session is required for payment.");
        }
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new PaymentException("Payment amount must be greater than zero.");
        }
        if (payment.getMethod() == null) {
            throw new PaymentException("Payment method is required.");
        }
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentType(Payment.PaymentType.SINGLE);
        payment.setPaymentDate(LocalDateTime.now());
        
        // Single payment update the session
        lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession session = payment.getSession();
        session.setPaymentStatus(lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.PaymentStatus.PAID);
        
        paymentDAO.save(payment);
        sessionDAO.update(session);
    }
    
    public void processUpfrontPayment(Payment payment, List<lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession> sessionsToPayFor) {
        if (payment.getPatient() == null) {
            throw new PaymentException("Patient is required for upfront payment.");
        }
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new PaymentException("Payment amount must be greater than zero.");
        }
        if (payment.getMethod() == null) {
            throw new PaymentException("Payment method is required.");
        }
        if (sessionsToPayFor == null || sessionsToPayFor.isEmpty()) {
            throw new PaymentException("At least one session must be selected for upfront payment.");
        }
        
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        // It's upfront if we are paying for multiple, otherwise could just be single
        payment.setPaymentDate(LocalDateTime.now());
        
        // Save the payment first to generate its ID
        paymentDAO.save(payment);
        
        // Link sessions and bulk update
        List<Long> sessionIds = new java.util.ArrayList<>();
        for (lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession ts : sessionsToPayFor) {
            ts.setUpfrontPayment(payment);
            ts.setPaymentStatus(lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.PaymentStatus.PAID);
            sessionDAO.update(ts);
            sessionIds.add(ts.getId());
        }
        
        // Using our new bulk update method
        // sessionDAO.bulkUpdatePaymentStatus(sessionIds, lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.PaymentStatus.PAID);
    }

    public void updatePayment(Payment payment) {
        paymentDAO.update(payment);
    }

    public void deletePayment(Payment payment) {
        paymentDAO.delete(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.getAllWithDetails();
    }

    public Payment getPaymentBySession(Long sessionId) {
        return paymentDAO.findBySession(sessionId);
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
}
