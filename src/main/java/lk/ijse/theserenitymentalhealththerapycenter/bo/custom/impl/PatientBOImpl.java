package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientTherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PaymentDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientTherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDeleteSummaryDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.PatientTherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientBOImpl implements PatientBO {
    private final PatientDAO patientDAO =
            (PatientDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PATIENT);
    private final PatientTherapyProgramDAO ptpDAO =
            (PatientTherapyProgramDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PATIENT_THERAPY_PROGRAM);
    private final PaymentDAO paymentDAO =
            (PaymentDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PAYMENT);

    @Override
    public Long registerPatient(PatientDTO patient) {
        if (!ValidationUtil.isValidName(patient.getName())) {
            throw new RegistrationException("Valid patient name is required.");
        }
        if (patient.getEmail() != null && !patient.getEmail().isEmpty()
                && !ValidationUtil.isValidEmail(patient.getEmail())) {
            throw new RegistrationException("Invalid email format.");
        }
        if (patient.getPhone() != null && !patient.getPhone().isEmpty()
                && !ValidationUtil.isValidPhone(patient.getPhone())) {
            throw new RegistrationException("Invalid phone number format.");
        }

        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) {
            Patient existing = patientDAO.findByEmail(patient.getEmail().trim());
            if (existing != null) {
                throw new RegistrationException("Email '" + patient.getEmail() + "' is already registered by another patient.");
            }
        }
        if (patient.getPhone() != null && !patient.getPhone().trim().isEmpty()) {
            Patient existing = patientDAO.findByPhone(patient.getPhone().trim());
            if (existing != null) {
                throw new RegistrationException("Phone number '" + patient.getPhone() + "' is already registered by another patient.");
            }
        }

        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient p = new Patient();
            p.setName(patient.getName());
            p.setEmail(patient.getEmail());
            p.setAddress(patient.getAddress());
            p.setPhone(patient.getPhone());
            p.setInterviewNote(patient.getInterviewNote());


            patientDAO.save(p, session);


            List<PatientTherapyProgram> enrollments = new ArrayList<>();
            Map<Long, Integer> upfrontMap = patient.getUpfrontSessionsPerProgram();

            for (TherapyProgramDTO programDto : patient.getPrograms()) {
                TherapyProgram programEntity = new TherapyProgram();
                programEntity.setId(programDto.getId());
                programEntity.setName(programDto.getName());
                programEntity.setDuration(programDto.getDuration());
                programEntity.setFee(programDto.getFee());
                programEntity.setTotalSessions(programDto.getTotalSessions());
                programEntity.setSessionFee(programDto.getSessionFee());
                programEntity.setDescription(programDto.getDescription());

                int upfrontSessions = 0;
                if (upfrontMap != null && upfrontMap.containsKey(programDto.getId())) {
                    upfrontSessions = upfrontMap.get(programDto.getId());
                }

                PatientTherapyProgram ptp = new PatientTherapyProgram(p, programEntity, upfrontSessions);
                enrollments.add(ptp);
            }

            if (!enrollments.isEmpty()) {
                for (PatientTherapyProgram ptp : enrollments) {
                    ptpDAO.save(ptp, session);
                }
            }


            if (patient.getUpfrontPayment() != null && patient.getUpfrontPayment().getAmount() != null) {
                Payment payment = new Payment();
                payment.setPatient(p);
                payment.setAmount(patient.getUpfrontPayment().getAmount());
                payment.setMethod(Payment.PaymentMethod.valueOf(patient.getUpfrontPayment().getMethod().name()));
                payment.setDiscount(patient.getUpfrontPayment().getDiscount());
                payment.setPaymentType(Payment.PaymentType.UPFRONT);
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setDescription(patient.getUpfrontPayment().getDescription());

                paymentDAO.save(payment, session);
                patient.getUpfrontPayment().setId(payment.getId());
            }

            transaction.commit();
            return p.getId();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RegistrationException("Failed to register patient: " + e.getMessage());
        }
    }

    @Override
    public void updatePatient(PatientDTO dto) {
        if (!ValidationUtil.isValidName(dto.getName())) {
            throw new RegistrationException("Valid patient name is required.");
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()
                && !ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Invalid email format.");
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()
                && !ValidationUtil.isValidPhone(dto.getPhone())) {
            throw new RegistrationException("Invalid phone number format.");
        }

        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            Patient existing = patientDAO.findByEmail(dto.getEmail().trim());
            if (existing != null && !existing.getId().equals(dto.getId())) {
                throw new RegistrationException("Email '" + dto.getEmail() + "' is already registered by another patient.");
            }
        }
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            Patient existing = patientDAO.findByPhone(dto.getPhone().trim());
            if (existing != null && !existing.getId().equals(dto.getId())) {
                throw new RegistrationException("Phone number '" + dto.getPhone() + "' is already registered by another patient.");
            }
        }

        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient entity = patientDAO.getById(dto.getId(), session);
            if (entity == null) throw new RegistrationException("Patient not found.");
            entity.setName(dto.getName());
            entity.setEmail(dto.getEmail());
            entity.setPhone(dto.getPhone());
            entity.setAddress(dto.getAddress());
            entity.setInterviewNote(dto.getInterviewNote());
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public void deletePatient(Long id) {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient entity = patientDAO.getById(id, session);
            if (entity == null) throw new RegistrationException("Patient not found.");
            patientDAO.delete(entity, session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public PatientDTO getPatientById(Long id) {
        Patient entity = patientDAO.getById(id);
        if (entity == null) throw new RegistrationException("Patient not found.");
        return toDTO(entity);
    }

    @Override
    public List<PatientDTO> getAllPatients() {
        return patientDAO.getAll().stream().map(this::toDTO).toList();
    }

    @Override
    public long getPatientCount() {
        return patientDAO.count();
    }

    @Override
    public List<PatientTherapyProgramDTO> getPatientPrograms(Long patientId) {
        return ptpDAO.findByPatient(patientId).stream().map(this::toPtpDTO).toList();
    }

    @Override
    public PatientTherapyProgramDTO getPatientProgram(Long patientId, Long programId) {
        PatientTherapyProgram entity = ptpDAO.findByPatientAndProgram(patientId, programId);
        return entity != null ? toPtpDTO(entity) : null;
    }

    @Override
    public void enrollPatientInProgram(Long patientId, Long programId, int upfrontSessions) {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient patient = patientDAO.getById(patientId, session);
            if (patient == null) throw new RegistrationException("Patient not found.");
            TherapyProgram program = new TherapyProgram();
            program.setId(programId);
            PatientTherapyProgram ptp = new PatientTherapyProgram(patient, program, upfrontSessions);
            ptpDAO.save(ptp, session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }


    @Override
    public PatientDeleteSummaryDTO getPatientDeleteSummary(Long patientId) {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient p = patientDAO.getById(patientId, session);
            if (p == null) throw new RegistrationException("Patient not found.");

            int programCount = p.getPatientTherapyPrograms() != null ? p.getPatientTherapyPrograms().size() : 0;
            int sessionCount = p.getSessions() != null ? p.getSessions().size() : 0;
            int paymentCount = p.getPayments() != null ? p.getPayments().size() : 0;
            double totalPaidAmount = 0.0;
            if (p.getPayments() != null) {
                totalPaidAmount = p.getPayments().stream()
                        .mapToDouble(pay -> pay.getAmount() != null ? pay.getAmount().doubleValue() : 0.0)
                        .sum();
            }

            transaction.commit();
            return new PatientDeleteSummaryDTO(
                    p.getName(),
                    programCount,
                    sessionCount,
                    paymentCount,
                    totalPaidAmount
            );
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Failed to retrieve patient summary: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PatientDTO> getPatientsWithNoScheduledSessions() {
        return patientDAO.getPatientsWithNoScheduledSessions().stream().map(this::toDTO).toList();
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

    public TherapyProgramDTO toDTO(TherapyProgram entity) {
        TherapyProgramDTO dto = new TherapyProgramDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDuration(entity.getDuration());
        dto.setFee(entity.getFee());
        dto.setTotalSessions(entity.getTotalSessions());
        dto.setSessionFee(entity.getSessionFee());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    private PatientTherapyProgramDTO toPtpDTO(PatientTherapyProgram entity) {
        PatientTherapyProgramDTO dto = new PatientTherapyProgramDTO();
        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        dto.setProgramId(entity.getProgram() != null ? entity.getProgram().getId() : null);
        dto.setSessionsPaid(entity.getSessionsPaid());
        dto.setSessionsUsed(entity.getSessionsUsed());
        dto.setProgram(entity.getProgram() != null ? toDTO(entity.getProgram()) : null);
        dto.setPatient(entity.getPatient() != null ? toDTO(entity.getPatient()) : null);
        // Display helpers
        if (entity.getProgram() != null) {
            dto.setProgramName(entity.getProgram().getName());
            dto.setTotalSessions(entity.getProgram().getTotalSessions() != null ? entity.getProgram().getTotalSessions() : 0);
        }
        return dto;
    }


}
