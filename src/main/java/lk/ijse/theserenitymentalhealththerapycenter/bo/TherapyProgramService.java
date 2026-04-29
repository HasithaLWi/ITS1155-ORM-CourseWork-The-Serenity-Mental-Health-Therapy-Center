package lk.ijse.theserenitymentalhealththerapycenter.bo;

import lk.ijse.theserenitymentalhealththerapycenter.dao.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SerenityException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;

public class TherapyProgramService {
    private final TherapyProgramDAO programDAO = new TherapyProgramDAO();

    public void saveProgram(TherapyProgram program) {
        validateProgram(program);
        programDAO.save(program);
    }

    public void updateProgram(TherapyProgram program) {
        validateProgram(program);
        programDAO.update(program);
    }

    public void deleteProgram(TherapyProgram program) {
        programDAO.delete(program);
    }

    public TherapyProgram getProgramById(Long id) {
        return programDAO.getById(id);
    }

    public List<TherapyProgram> getAllPrograms() {
        return programDAO.getAll();
    }

    public List<TherapyProgram> searchPrograms(String name) {
        return programDAO.searchByName(name);
    }

    public long getProgramCount() {
        return programDAO.count();
    }

    private void validateProgram(TherapyProgram program) {
        if (!ValidationUtil.isNotEmpty(program.getName())) {
            throw new SerenityException("Program name is required.");
        }
        if (program.getFee() != null && program.getFee().signum() < 0) {
            throw new SerenityException("Program fee cannot be negative.");
        }
    }
}
