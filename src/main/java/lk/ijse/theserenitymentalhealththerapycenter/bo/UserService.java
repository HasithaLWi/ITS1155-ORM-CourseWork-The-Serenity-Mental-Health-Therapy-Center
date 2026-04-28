package lk.ijse.theserenitymentalhealththerapycenter.bo;

import lk.ijse.theserenitymentalhealththerapycenter.dao.UserDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import lk.ijse.theserenitymentalhealththerapycenter.exception.LoginException;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PasswordResetException;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.PasswordUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();


    public void createUserForFirstTime(){
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("$2a$12$N4OtyqkB/wBo36vEuQEVFui1PuLF8k.iEQ.Sv2KE3NPdNuIH1weQi");
        admin.setFullName("Admin User");
        admin.setEmail("hasithawijesinghe@gmail.com");
        admin.setRole(User.Role.ADMIN);
        admin.setSecurityQuestion("What is your favorite color?");
        admin.setSecurityAnswer(PasswordUtil.hashPassword("blue"));

        userDAO.createAdminUser(admin);
    }

    /**
     * Authenticate a user with username and plain-text password.
     * Uses BCrypt to verify the password against the stored hash.
     */
    public User login(String username, String plainTextPassword) {
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

        return user;
    }

    /**
     * Register a new user. Password and security answer are hashed with BCrypt before storing.
     */
    public void register(String username, String plainTextPassword, String fullName,
                          String email, User.Role role,
                          String securityQuestion, String securityAnswer) {
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
        if (!ValidationUtil.isNotEmpty(securityQuestion)) {
            throw new RegistrationException("Security question is required.");
        }
        if (!ValidationUtil.isNotEmpty(securityAnswer)) {
            throw new RegistrationException("Security answer is required.");
        }

        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            throw new RegistrationException("Username '" + username + "' is already taken.");
        }

        // Check if email already exists
        if (email != null && !email.isEmpty() && userDAO.emailExists(email)) {
            throw new RegistrationException("Email '" + email + "' is already registered.");
        }

        // Create and save user with hashed password and security answer
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hashPassword(plainTextPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswer(PasswordUtil.hashPassword(securityAnswer.toLowerCase().trim()));

        userDAO.save(user);
    }

    /**
     * Overloaded register for backward compatibility (without security question).
     */
    public void register(String username, String plainTextPassword, String fullName,
                          String email, User.Role role) {
        register(username, plainTextPassword, fullName, email, role, null, null);
    }

    /**
     * Verify a user's identity for password reset using username and email.
     * Returns the user if found, otherwise throws an exception.
     */
    public User verifyIdentity(String username, String email) {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(email)) {
            throw new PasswordResetException("Username and email are required.");
        }

        User user = userDAO.findByUsernameAndEmail(username, email);
        if (user == null) {
            throw new PasswordResetException("No account found with that username and email combination.");
        }

        if (user.getSecurityQuestion() == null || user.getSecurityQuestion().isEmpty()) {
            throw new PasswordResetException("No security question set for this account. Contact an administrator.");
        }

        return user;
    }

    /**
     * Verify the security answer for a user.
     */
    public boolean verifySecurityAnswer(User user, String answer) {
        if (user == null || !ValidationUtil.isNotEmpty(answer)) {
            return false;
        }
        return PasswordUtil.verifyPassword(answer.toLowerCase().trim(), user.getSecurityAnswer());
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

    public List<User> getAllUsers() {
        return userDAO.getAll();
    }

    public void updateUser(User user) {
        userDAO.update(user);
    }

    public void deleteUser(User user) {
        userDAO.delete(user);
    }
}
