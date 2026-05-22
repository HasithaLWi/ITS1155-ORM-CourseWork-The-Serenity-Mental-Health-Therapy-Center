package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.UserDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import lk.ijse.theserenitymentalhealththerapycenter.exception.LoginException;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PasswordResetException;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.PasswordUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserBOImpl implements UserBO {
    private final UserDAO userDAO =
            (UserDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.USER);

    public void createUserForFirstTime() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("$2a$12$N4OtyqkB/wBo36vEuQEVFui1PuLF8k.iEQ.Sv2KE3NPdNuIH1weQi");
        admin.setFullName("Admin User");
        admin.setEmail("hasithawijesinghe@gmail.com");
        admin.setRole(User.Role.ADMIN);
        userDAO.createAdminUser(admin);
    }

    public UserDTO login(String username, String plainTextPassword) {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(plainTextPassword)) {
            throw new LoginException("Username and password are required.");
        }
        User user = userDAO.findByUsername(username);
        if (user == null) throw new LoginException("Invalid username or password.");
        if (!PasswordUtil.verifyPassword(plainTextPassword, user.getPassword())) {
            throw new LoginException("Invalid username or password.");
        }
        return toDTO(user);
    }

    public void register(String username, String plainTextPassword, String fullName, String email, UserRole role) {
        if (!ValidationUtil.isNotEmpty(username)) throw new RegistrationException("Username is required.");
        if (!ValidationUtil.isNotEmpty(plainTextPassword) || plainTextPassword.length() < 6)
            throw new RegistrationException("Password must be at least 6 characters.");
        if (!ValidationUtil.isNotEmpty(fullName)) throw new RegistrationException("Full name is required.");
        if (email != null && !email.isEmpty() && !ValidationUtil.isValidEmail(email))
            throw new RegistrationException("Invalid email format.");
        if (userDAO.usernameExists(username))
            throw new RegistrationException("Username '" + username + "' is already taken.");
        if (email != null && !email.isEmpty() && userDAO.emailExists(email))
            throw new RegistrationException("Email '" + email + "' is already registered.");

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(PasswordUtil.hashPassword(plainTextPassword));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setRole(User.Role.valueOf(role.name()));
            userDAO.save(user, session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public UserDTO verifyIdentity(String username, String email) {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(email))
            throw new PasswordResetException("Username and email are required.");
        User user = userDAO.findByUsernameAndEmail(username, email);
        if (user == null) throw new PasswordResetException("No account found with that username and email combination.");
        return toDTO(user);
    }

    public void resetPassword(String username, String newPassword) {
        if (!ValidationUtil.isNotEmpty(newPassword) || newPassword.length() < 6)
            throw new PasswordResetException("New password must be at least 6 characters.");

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            User user = userDAO.findByUsername(username);
            if (user == null) throw new PasswordResetException("User not found.");
            // Re-attach and update via setters
            User managed = userDAO.getById(user.getId(), session);
            managed.setPassword(PasswordUtil.hashPassword(newPassword));
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public boolean emailExists(String email) { return userDAO.emailExists(email); }
    public boolean usernameExists(String username) { return userDAO.usernameExists(username); }

    public List<UserDTO> getAllUsers() {
        return userDAO.getAll().stream().map(this::toDTO).toList();
    }

    public void updateUser(UserDTO dto) {
        if (!ValidationUtil.isNotEmpty(dto.getUsername())) {
            throw new RegistrationException("Username is required.");
        }
        if (!ValidationUtil.isNotEmpty(dto.getFullName())) {
            throw new RegistrationException("Full name is required.");
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && !ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Invalid email format.");
        }

        User otherByUsername = userDAO.findByUsername(dto.getUsername().trim());
        if (otherByUsername != null && !otherByUsername.getId().equals(dto.getId())) {
            throw new RegistrationException("Username '" + dto.getUsername() + "' is already taken.");
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            User otherByEmail = userDAO.findByEmail(dto.getEmail().trim());
            if (otherByEmail != null && !otherByEmail.getId().equals(dto.getId())) {
                throw new RegistrationException("Email '" + dto.getEmail() + "' is already taken.");
            }
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            User entity = userDAO.getById(dto.getId(), session);
            if (entity == null) throw new RegistrationException("User not found.");
            entity.setUsername(dto.getUsername().trim());
            entity.setFullName(dto.getFullName().trim());
            entity.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
            if (dto.getRole() != null) entity.setRole(User.Role.valueOf(dto.getRole().name()));
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void updateUserWithPassword(UserDTO dto, String newPlainPassword) {
        if (!ValidationUtil.isNotEmpty(dto.getUsername())) {
            throw new RegistrationException("Username is required.");
        }
        if (!ValidationUtil.isNotEmpty(dto.getFullName())) {
            throw new RegistrationException("Full name is required.");
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && !ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Invalid email format.");
        }
        if (!ValidationUtil.isNotEmpty(newPlainPassword) || newPlainPassword.length() < 6) {
            throw new RegistrationException("Password must be at least 6 characters.");
        }

        User otherByUsername = userDAO.findByUsername(dto.getUsername().trim());
        if (otherByUsername != null && !otherByUsername.getId().equals(dto.getId())) {
            throw new RegistrationException("Username '" + dto.getUsername() + "' is already taken.");
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            User otherByEmail = userDAO.findByEmail(dto.getEmail().trim());
            if (otherByEmail != null && !otherByEmail.getId().equals(dto.getId())) {
                throw new RegistrationException("Email '" + dto.getEmail() + "' is already taken.");
            }
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            User entity = userDAO.getById(dto.getId(), session);
            if (entity == null) throw new RegistrationException("User not found.");
            entity.setUsername(dto.getUsername().trim());
            entity.setFullName(dto.getFullName().trim());
            entity.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
            entity.setPassword(PasswordUtil.hashPassword(newPlainPassword));
            if (dto.getRole() != null) entity.setRole(User.Role.valueOf(dto.getRole().name()));
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void updateProfile(UserDTO dto, String currentPassword, String newPlainPassword) {
        if (!ValidationUtil.isNotEmpty(currentPassword)) {
            throw new RegistrationException("Current password is required to verify changes.");
        }
        if (!ValidationUtil.isNotEmpty(dto.getUsername())) {
            throw new RegistrationException("Username is required.");
        }
        if (!ValidationUtil.isNotEmpty(dto.getFullName())) {
            throw new RegistrationException("Full name is required.");
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && !ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Invalid email format.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            User entity = userDAO.getById(dto.getId(), session);
            if (entity == null) throw new RegistrationException("User not found.");

            // Verify current password
            if (!PasswordUtil.verifyPassword(currentPassword, entity.getPassword())) {
                throw new RegistrationException("Incorrect current password.");
            }

            // Check if updated username is already taken by another user
            User otherByUsername = userDAO.findByUsername(dto.getUsername());
            if (otherByUsername != null && !otherByUsername.getId().equals(entity.getId())) {
                throw new RegistrationException("Username '" + dto.getUsername() + "' is already taken.");
            }

            // Check if updated email is already taken by another user
            if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
                User otherByEmail = userDAO.findByEmail(dto.getEmail());
                if (otherByEmail != null && !otherByEmail.getId().equals(entity.getId())) {
                    throw new RegistrationException("Email '" + dto.getEmail() + "' is already taken.");
                }
            }

            entity.setUsername(dto.getUsername());
            entity.setFullName(dto.getFullName());
            entity.setEmail(dto.getEmail());

            if (newPlainPassword != null && !newPlainPassword.trim().isEmpty()) {
                if (newPlainPassword.length() < 6) {
                    throw new RegistrationException("New password must be at least 6 characters.");
                }
                entity.setPassword(PasswordUtil.hashPassword(newPlainPassword));
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void deleteUser(Long id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            User entity = userDAO.getById(id, session);
            if (entity == null) throw new RegistrationException("User not found.");
            userDAO.delete(entity, session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public List<String> getAdminEmail() {
        List<User> admin = userDAO.getAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .toList();
        if (!admin.isEmpty()) {
            return admin.stream().map(User::getEmail).toList();
        }
        return null;
    }

    private UserDTO toDTO(User entity) {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setFullName(entity.getFullName());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole() != null ? UserRole.valueOf(entity.getRole().name()) : null);
        return dto;
    }
}
