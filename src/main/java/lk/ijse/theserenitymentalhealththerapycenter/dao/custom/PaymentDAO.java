package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentDAO extends CrudDAO<Payment> {
    Payment findBySession(Long sessionId);
    List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);
    List<Payment> getAllWithDetails();
    List<Payment> findByPatient(Long patientId);
    Payment findUpfrontByPatient(Long patientId);
    Payment findUpfrontByPatient(Long patientId, Session session);
    List<Payment> findByPatientAndDateRange(Long patientId, LocalDateTime start, LocalDateTime end);
    List<Payment> findByType(Payment.PaymentType type);
    List<Payment> findFiltered(Long patientId, LocalDateTime start, LocalDateTime end, Payment.PaymentType type);
}
