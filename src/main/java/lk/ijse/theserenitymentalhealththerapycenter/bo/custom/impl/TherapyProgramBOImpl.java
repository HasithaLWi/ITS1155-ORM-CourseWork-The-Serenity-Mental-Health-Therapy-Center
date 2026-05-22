package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.QueryDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.TherapistStatus;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SerenityException;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.ProgramSummaryTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class TherapyProgramBOImpl implements TherapyProgramBO {

    private final TherapyProgramDAO programDAO = (TherapyProgramDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.THERAPY_PROGRAM);
    private final QueryDAO queryDAO = (QueryDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.QUERY);
    private final TherapySessionDAO sessionDAO = (TherapySessionDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.THERAPY_SESSION);

    @Override
    public void saveProgram(TherapyProgramDTO dto) {
        validateProgram(dto);
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            programDAO.save(toEntity(dto), session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw e;
        }
    }

    @Override
    public void updateProgram(TherapyProgramDTO dto) {
        validateProgram(dto);
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            TherapyProgram entity = programDAO.getById(dto.getId(), session);
            if (entity == null)
                throw new SerenityException("Program not found.");
            entity.setName(dto.getName());
            entity.setDuration(dto.getDuration());
            entity.setFee(dto.getFee());
            entity.setTotalSessions(dto.getTotalSessions());
            entity.setSessionFee(dto.getSessionFee());
            entity.setDescription(dto.getDescription());
            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw e;
        }
    }

    @Override
    public void deleteProgram(Long id) {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            TherapyProgram entity = programDAO.getById(id, session);
            if (entity == null)
                throw new SerenityException("Program not found.");

            for (Therapist therapist : entity.getTherapists()) {
                therapist.getPrograms().remove(entity);
            }
            entity.getTherapists().clear();

            List<TherapySession> therapySessions = sessionDAO.getAll();
            for (TherapySession therapySession : therapySessions) {
                if (therapySession.getProgram().getId().equals(id)) {
                    sessionDAO.delete(therapySession, session);
                }
            }

            if (entity.getPatientTherapyPrograms() != null && !entity.getPatientTherapyPrograms().isEmpty()) {
                throw new SerenityException("Cannot delete program with enrolled patients.");
            }
            programDAO.delete(entity, session);

            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw e;
        }
    }

    @Override
    public TherapyProgramDTO getProgramById(Long id) {
        TherapyProgram entity = programDAO.getById(id);
        if (entity == null)
            throw new SerenityException("Program not found with ID: " + id);
        return toDTO(entity);
    }

    @Override
    public List<TherapyProgramDTO> getAllPrograms() {
        return programDAO.getAll().stream().map(this::toDTO).toList();
    }

    @Override
    public long getProgramCount() {
        return programDAO.count();
    }

    @Override
    public List<ProgramSummaryTM> getProgramSummaries() {
        return queryDAO.getProgramSummaries().stream().map(dto -> new ProgramSummaryTM(
                dto.getName(),
                dto.getDuration(),
                dto.getFee(),
                dto.getEnrolledPatients()))
                .toList();
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
        dto.setTherapists(entity.getTherapists().stream().map(t -> new TherapistDTO(t.getId(), t.getName(),
                t.getSpecialty(), t.getPhone(), t.getEmail(),
                t.getStatus().equals(Therapist.Status.ACTIVE) ? TherapistStatus.ACTIVE : TherapistStatus.INACTIVE))
                .toList());
        return dto;
    }

    public TherapyProgram toEntity(TherapyProgramDTO dto) {
        TherapyProgram entity = new TherapyProgram();
        if (dto.getId() > 0)
            entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDuration(dto.getDuration());
        entity.setFee(dto.getFee());
        entity.setTotalSessions(dto.getTotalSessions());
        entity.setSessionFee(dto.getSessionFee());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    private void validateProgram(TherapyProgramDTO program) {
        if (!ValidationUtil.isNotEmpty(program.getName()))
            throw new SerenityException("Program name is required.");
        if (program.getFee() != null && program.getFee().signum() < 0)
            throw new SerenityException("Program fee cannot be negative.");
    }
}
