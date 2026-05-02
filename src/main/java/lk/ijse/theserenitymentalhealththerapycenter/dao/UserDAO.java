package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UserDAO extends GenericDAO<User> {

    public UserDAO() {
        super(User.class);
    }

    public void createAdminUser(User adminUser) {
        if (findByUsername("admin") == null) {
            save(adminUser);
        }
    }

    /**
     * Find a user by their username (HQL query).
     */
    public User findByUsername(String username) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        }
    }

    /**
     * Find a user by their email (HQL query).
     */
    public User findByEmail(String email) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }

    /**
     * Check if a username already exists.
     */
    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    /**
     * Check if an email is already registered.
     */
    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    /**
     * Find a user by both username and email (for forgot-password identity verification).
     */
    public User findByUsernameAndEmail(String username, String email) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.username = :username AND u.email = :email", User.class);
            query.setParameter("username", username);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }
}
