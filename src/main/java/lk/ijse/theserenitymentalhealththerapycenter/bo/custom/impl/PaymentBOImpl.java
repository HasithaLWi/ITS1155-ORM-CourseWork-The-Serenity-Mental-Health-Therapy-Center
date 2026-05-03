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
        payment.setPaymentDate(LocalDateTime.now());
        paymentDAO.save(payment);
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
