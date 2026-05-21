package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public interface TherapySessionDAO extends CrudDAO<TherapySession> {

    List<TherapySession> findByDate(LocalDate date);
    List<TherapySession> findByPatient(Long patientId);
    List<TherapySession> findByTherapist(Long therapistId);
    List<TherapySession> findByDateRange(LocalDate startDate, LocalDate endDate);
    long countByDate(LocalDate date);
    List<TherapySession> getAllWithDetails();
    List<TherapySession> findByPatientAndProgram(Long patientId, Long programId);
    List<TherapySession> findUnscheduledByPatient(Long patientId);
    long countCompletedByPatientAndProgram(Long patientId, Long programId);
    long countByPatientAndProgram(Long patientId, Long programId);
    List<TherapySession> findByTherapistAndDate(Long therapistId, LocalDate date);
    void bulkUpdatePaymentStatus(List<Long> sessionIds, TherapySession.PaymentStatus paymentStatus, Session session);
    void saveAll(List<TherapySession> sessions, Session session);
}
