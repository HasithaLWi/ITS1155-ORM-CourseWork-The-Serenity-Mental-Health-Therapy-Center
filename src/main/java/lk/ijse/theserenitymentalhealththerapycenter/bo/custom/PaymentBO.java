package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentBO extends SuperBO {
    void processPayment(PaymentDTO dto);
    void processUpfrontPayment(PaymentDTO dto, List<Long> sessionIds);
    void processSessionPayment(PaymentDTO dto, Long sessionId);
    void saveRegistrationPayment(PaymentDTO dto);
    void updatePayment(PaymentDTO dto);
    void deletePayment(Long id);
    List<PaymentDTO> getAllPayments();
    PaymentDTO getPaymentBySession(Long sessionId);
    BigDecimal getMonthlyRevenue();
    BigDecimal getTotalRevenue();
    long getPaymentCount();
    void processExpense(PaymentDTO dto);
    List<PaymentDTO> getFilteredPayments(Long patientId, LocalDateTime start, LocalDateTime end, String paymentType);
    List<PaymentDTO> getPaymentsByPatient(Long patientId);
}
