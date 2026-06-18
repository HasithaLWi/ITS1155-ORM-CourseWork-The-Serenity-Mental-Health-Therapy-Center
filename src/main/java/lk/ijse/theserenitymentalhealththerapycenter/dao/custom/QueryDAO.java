package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.SuperDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.ProgramSummaryDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.ProgramSummaryTM;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;

import java.util.List;

public interface QueryDAO extends SuperDAO {
    List<ProgramSummaryDTO> getProgramSummaries();
}
