package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;

import java.util.List;

public interface PatientDAO extends CrudDAO<Patient> {
    List<Patient> searchByName(String name);
}
