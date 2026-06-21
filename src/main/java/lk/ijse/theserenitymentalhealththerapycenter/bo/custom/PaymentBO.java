package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;

import lk.ijse.theserenitymentalhealththerapycenter.enumaration.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentBO extends SuperBO {
    void processPayment(PaymentDTO dto);
    void processUpfrontPayment(PaymentDTO dto, List<Long> sessionIds);
    void processSessionPayment(PaymentDTO dto, Long sessionId);
    Long processMultipleSessionPayment(Long patientId, Long programId, int sessionCount, BigDecimal amount, PaymentMethod method);
    void saveRegistrationPayment(PaymentDTO dto);
    void updatePayment(PaymentDTO dto);
    void deletePayment(Long id);
    List<PaymentDTO> getAllPayments();
    BigDecimal getMonthlyRevenue();
    BigDecimal getTotalRevenue();
    void processExpense(PaymentDTO dto);
    List<PaymentDTO> getFilteredPayments(Long patientId, LocalDateTime start, LocalDateTime end, String paymentType);
    List<PaymentDTO> getPaymentsByPatient(Long patientId);
}
