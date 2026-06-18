package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.QueryDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.ProgramSummaryDTO;

import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import org.hibernate.Session;

import java.util.List;

public class QueryDAOImpl implements QueryDAO {
    @Override
    public List<ProgramSummaryDTO> getProgramSummaries() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery(
                    "SELECT new ProgramSummaryDTO(tp.name, tp.duration, tp.fee, COUNT(ptp.id)) " +
                            "FROM TherapyProgram tp " +
                            "LEFT JOIN tp.patientTherapyPrograms ptp " +
                            "GROUP BY tp.id, tp.name, tp.duration, tp.fee " +
                            "ORDER BY COUNT(ptp.id) DESC",
                    ProgramSummaryDTO.class
            ).list();
        }
    }


}
