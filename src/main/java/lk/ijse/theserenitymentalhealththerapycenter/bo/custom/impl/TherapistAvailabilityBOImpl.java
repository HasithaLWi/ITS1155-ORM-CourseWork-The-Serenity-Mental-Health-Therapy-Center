package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistAvailabilityBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistAvailabilityDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistAvailabilityDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapistAvailability;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TherapistAvailabilityBOImpl implements TherapistAvailabilityBO {
    private final TherapistAvailabilityDAO therapistAvailabilityDAO = (TherapistAvailabilityDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPIST_AVAILABILITY);

    private final TherapistDAO therapistDAO = (TherapistDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPIST);
    @Override
    public List<TherapistAvailabilityDTO> getAllAvailabilityByTherapist(Long id) {
        return therapistAvailabilityDAO.getAllByTherapist(id).stream()
                .map(availability -> new TherapistAvailabilityDTO(
                        availability.getId(),
                        availability.getDayOfWeek(),
                        availability.getTime(),
                        availability.getTherapist().getId()
                )).toList();
    }

    @Override
    public void saveAvailability(TherapistAvailabilityDTO therapistAvailabilityDTO) {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            therapistAvailabilityDAO.save(new TherapistAvailability(
                    therapistAvailabilityDTO.getId(),
                    therapistAvailabilityDTO.getDayOfWeek(),
                    therapistAvailabilityDTO.getTime(),
                    therapistDAO.getById(therapistAvailabilityDTO.getTherapistId())

            ), session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }

    }

    @Override
    public void deleteAvailabilityById(Long id) {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try{
            TherapistAvailability therapistAvailability = therapistAvailabilityDAO.getById(id, session);
            therapistAvailabilityDAO.delete(therapistAvailability, session);
            transaction.commit();

        }  catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean updateAvailability(TherapistAvailabilityDTO therapistAvailabilityDTO) {
        return false;
    }
}
