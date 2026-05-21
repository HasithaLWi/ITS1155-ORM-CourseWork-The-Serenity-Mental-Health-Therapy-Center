package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;

import java.util.List;

public interface UserBO extends SuperBO {
    void createUserForFirstTime();
    UserDTO login(String username, String plainTextPassword);
    void register(String username, String plainTextPassword, String fullName, String email, UserRole role);
    UserDTO verifyIdentity(String username, String email);
    void resetPassword(String username, String newPassword);
    boolean emailExists(String email);
    boolean usernameExists(String username);
    List<UserDTO> getAllUsers();
    void updateUser(UserDTO dto);
    void deleteUser(Long id);
}
