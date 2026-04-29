package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TherapistDAO extends GenericDAO<Therapist> {

    public TherapistDAO() {
        super(Therapist.class);
    }

    /**
     * Search therapists by name (case-insensitive LIKE query).
     */
    public List<Therapist> searchByName(String name) {
        try (Session session = HibernateUtil.getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE LOWER(t.name) LIKE LOWER(:name)", Therapist.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }

    /**
     * Find therapists by their status.
     */
    public List<Therapist> findByStatus(Therapist.Status status) {
        try (Session session = HibernateUtil.getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE t.status = :status", Therapist.class);
            query.setParameter("status", status);
            return query.list();
        }
    }

    /**
     * Find therapists by specialty (case-insensitive).
     */
    public List<Therapist> findBySpecialty(String specialty) {
        try (Session session = HibernateUtil.getSession()) {
            Query<Therapist> query = session.createQuery(
                    "FROM Therapist t WHERE LOWER(t.specialty) LIKE LOWER(:specialty)", Therapist.class);
            query.setParameter("specialty", "%" + specialty + "%");
            return query.list();
        }
    }
}
