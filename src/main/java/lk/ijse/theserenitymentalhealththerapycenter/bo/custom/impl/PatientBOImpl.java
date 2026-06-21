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
        // --- Validations ---
        validatePatientData(patient, null);

        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient p = new Patient();
            mapDtoToEntity(patient, p);
            p.setStatus("ACTIVE"); // Always ACTIVE on registration

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
        // --- Validations ---
        validatePatientData(dto, dto.getId());

        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            Patient entity = patientDAO.getById(dto.getId(), session);
            if (entity == null) throw new RegistrationException("Patient not found.");
            mapDtoToEntity(dto, entity);
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
                    p.getFullName(),
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


    // ============ Private Helper Methods ============

    /**
     * Validates patient data with comprehensive rules.
     * @param dto the patient data to validate
     * @param existingId the ID of the patient being updated (null for new registration)
     */
    private void validatePatientData(PatientDTO dto, Long existingId) {
        if (!ValidationUtil.isValidName(dto.getFirstName())) {
            throw new RegistrationException("First name is required (min 2 chars, letters, spaces, hyphens, and dots only).");
        }
        if (!ValidationUtil.isValidName(dto.getLastName())) {
            throw new RegistrationException("Last name is required (min 2 chars, letters, spaces, hyphens, and dots only).");
        }

        // Date of birth validation (optional field)
        if (dto.getDateOfBirth() != null && !ValidationUtil.isValidDateOfBirth(dto.getDateOfBirth())) {
            throw new RegistrationException("Date of birth must be a valid past date (patient must be at least 1 year old).");
        }

        // Email validation and uniqueness
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (!ValidationUtil.isValidEmail(dto.getEmail())) {
                throw new RegistrationException("Invalid email format.");
            }
            Patient existingByEmail = patientDAO.findByEmail(dto.getEmail().trim());
            if (existingByEmail != null && (existingId == null || !existingByEmail.getId().equals(existingId))) {
                throw new RegistrationException("Email '" + dto.getEmail() + "' is already registered by another patient.");
            }
        }

        // Phone validation and uniqueness
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            if (!ValidationUtil.isValidPhone(dto.getPhone())) {
                throw new RegistrationException("Invalid phone number format.");
            }
            Patient existingByPhone = patientDAO.findByPhone(dto.getPhone().trim());
            if (existingByPhone != null && (existingId == null || !existingByPhone.getId().equals(existingId))) {
                throw new RegistrationException("Phone number '" + dto.getPhone() + "' is already registered by another patient.");
            }
        }

        // Emergency contact: if phone is given, name must be provided
        if (ValidationUtil.isNotEmpty(dto.getEmergencyContactPhone())) {
            if (!ValidationUtil.isValidPhone(dto.getEmergencyContactPhone())) {
                throw new RegistrationException("Invalid emergency contact phone number format.");
            }
            if (!ValidationUtil.isNotEmpty(dto.getEmergencyContactName())) {
                throw new RegistrationException("Emergency contact name is required when phone number is provided.");
            }
        }

        // Insurance policy ID validation (if provided)
        if (ValidationUtil.isNotEmpty(dto.getInsurancePolicyId())) {
            if (!ValidationUtil.isValidInsurancePolicyId(dto.getInsurancePolicyId())) {
                throw new RegistrationException("Invalid insurance policy ID format (alphanumeric with hyphens, 3-30 characters).");
            }
        }
    }

    /**
     * Maps all fields from DTO to Entity.
     */
    private void mapDtoToEntity(PatientDTO dto, Patient entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setGenderIdentity(dto.getGenderIdentity());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setEmergencyContactName(dto.getEmergencyContactName());
        entity.setEmergencyContactRelationship(dto.getEmergencyContactRelationship());
        entity.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        entity.setInsuranceProvider(dto.getInsuranceProvider());
        entity.setInsurancePolicyId(dto.getInsurancePolicyId());
        entity.setInsuranceGroupNumber(dto.getInsuranceGroupNumber());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        entity.setInterviewNote(dto.getInterviewNote());
    }

    /**
     * Maps all fields from Entity to DTO.
     */
    private PatientDTO toDTO(Patient entity) {
        PatientDTO dto = new PatientDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGenderIdentity(entity.getGenderIdentity());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setEmergencyContactName(entity.getEmergencyContactName());
        dto.setEmergencyContactRelationship(entity.getEmergencyContactRelationship());
        dto.setEmergencyContactPhone(entity.getEmergencyContactPhone());
        dto.setInsuranceProvider(entity.getInsuranceProvider());
        dto.setInsurancePolicyId(entity.getInsurancePolicyId());
        dto.setInsuranceGroupNumber(entity.getInsuranceGroupNumber());
        dto.setStatus(entity.getStatus());
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
