package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import org.hibernate.Session;

import java.util.List;

public interface PatientTherapyProgramDAO extends CrudDAO<PatientTherapyProgram> {
    List<PatientTherapyProgram> findByPatient(Long patientId);
    PatientTherapyProgram findByPatientAndProgram(Long patientId, Long programId);
    void deductCredit(Long patientId, Long programId, Session session);
    void saveAll(List<PatientTherapyProgram> enrollments, Session session);
}
