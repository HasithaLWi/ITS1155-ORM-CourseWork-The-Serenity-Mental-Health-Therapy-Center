package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.UserDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import lk.ijse.theserenitymentalhealththerapycenter.exception.LoginException;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PasswordResetException;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.PasswordUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;

public class UserBOImpl implements UserBO {
    private final UserDAOImpl userDAO = new UserDAOImpl();


    public void createUserForFirstTime(){
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("$2a$12$N4OtyqkB/wBo36vEuQEVFui1PuLF8k.iEQ.Sv2KE3NPdNuIH1weQi");
        admin.setFullName("Admin User");
        admin.setEmail("hasithawijesinghe@gmail.com");
        admin.setRole(User.Role.ADMIN);

        userDAO.createAdminUser(admin);
    }

    /**
     * Authenticate a user with username and plain-text password.
     * Uses BCrypt to verify the password against the stored hash.
     */
    public UserDTO login(String username, String plainTextPassword) {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(plainTextPassword)) {
            throw new LoginException("Username and password are required.");
        }

        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new LoginException("Invalid username or password.");
        }

        if (!PasswordUtil.verifyPassword(plainTextPassword, user.getPassword())) {
            throw new LoginException("Invalid username or password.");
        }

        return toDTO(user);
    }

    /**
     * Register a new user. Password is hashed with BCrypt before storing.
     */
    public void register(String username, String plainTextPassword, String fullName,
                          String email, UserRole role) {
        // Validations
        if (!ValidationUtil.isNotEmpty(username)) {
            throw new RegistrationException("Username is required.");
        }
        if (!ValidationUtil.isNotEmpty(plainTextPassword) || plainTextPassword.length() < 6) {
            throw new RegistrationException("Password must be at least 6 characters.");
        }
        if (!ValidationUtil.isNotEmpty(fullName)) {
            throw new RegistrationException("Full name is required.");
        }
        if (email != null && !email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
            throw new RegistrationException("Invalid email format.");
        }

        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            throw new RegistrationException("Username '" + username + "' is already taken.");
        }

        // Check if email already exists
        if (email != null && !email.isEmpty() && userDAO.emailExists(email)) {
            throw new RegistrationException("Email '" + email + "' is already registered.");
        }

        // Create and save user with hashed password
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hashPassword(plainTextPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(User.Role.valueOf(role.name()));

        userDAO.save(user);
    }

    /**
     * Verify a user's identity for password reset using username and email.
     */
    public UserDTO verifyIdentity(String username, String email) {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(email)) {
            throw new PasswordResetException("Username and email are required.");
        }

        User user = userDAO.findByUsernameAndEmail(username, email);
        if (user == null) {
            throw new PasswordResetException("No account found with that username and email combination.");
        }

        return toDTO(user);
    }

    /**
     * Reset a user's password.
     */
    public void resetPassword(String username, String newPassword) {
        if (!ValidationUtil.isNotEmpty(newPassword) || newPassword.length() < 6) {
            throw new PasswordResetException("New password must be at least 6 characters.");
        }

        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new PasswordResetException("User not found.");
        }

        user.setPassword(PasswordUtil.hashPassword(newPassword));
        userDAO.update(user);
    }

    /**
     * Check if an email is already registered.
     */
    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }

    /**
     * Check if a username already exists.
     */
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }

    public List<UserDTO> getAllUsers() {
        return userDAO.getAll().stream().map(this::toDTO).toList();
    }

    public void updateUser(UserDTO dto) {
        User entity = userDAO.getById(dto.getId());
        if (entity == null) throw new RegistrationException("User not found.");
        entity.setUsername(dto.getUsername());
        entity.setFullName(dto.getFullName());
        entity.setEmail(dto.getEmail());
        if (dto.getRole() != null) {
            entity.setRole(User.Role.valueOf(dto.getRole().name()));
        }
        userDAO.update(entity);
    }

    public void deleteUser(Long id) {
        User entity = userDAO.getById(id);
        if (entity == null) throw new RegistrationException("User not found.");
        userDAO.delete(entity);
    }

    // ==================== Conversion Helpers ====================

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
