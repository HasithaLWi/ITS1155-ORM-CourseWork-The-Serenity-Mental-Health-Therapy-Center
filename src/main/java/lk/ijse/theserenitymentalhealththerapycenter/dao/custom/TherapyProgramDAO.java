package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;

import java.util.List;

public interface TherapyProgramDAO extends CrudDAO<TherapyProgram> {
    List<TherapyProgram> searchByName(String name);
}
