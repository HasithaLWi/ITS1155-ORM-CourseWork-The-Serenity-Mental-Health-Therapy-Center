package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.*;

public class DAOFactory {

    private static DAOFactory daoFactory;

    private DAOFactory() {
    }

    public static DAOFactory getInstance() {
        if (daoFactory == null) {
            daoFactory = new DAOFactory();
        }
        return daoFactory;
    }

    public SuperDAO getDAO(DAOType daoType) {
        return switch (daoType) {
            case PATIENT -> new PatientDAOImpl();
            case THERAPIST -> new TherapistDAOImpl();
            case PAYMENT -> new PaymentDAOImpl();
            case THERAPY_PROGRAM -> new TherapyProgramDAOImpl();
            case THERAPY_SESSION -> new TherapySessionDAOImpl();
            case USER -> new UserDAOImpl();
            default -> null;
        };

    }

    public enum DAOType {
        PATIENT, THERAPIST, THERAPY_SESSION, THERAPY_PROGRAM, PAYMENT ,USER
    }
}
