package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;

public interface UserDAO extends CrudDAO<User> {
    void createAdminUser(User adminUser);
    User findByUsername(String username);
    User findByEmail(String email);
    boolean usernameExists(String username);
    boolean emailExists(String email);
    User findByUsernameAndEmail(String username, String email);
}
