package lk.ijse.theserenitymentalhealththerapycenter.bo;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.*;

public class BOFactory {
    private static BOFactory boFactory;
    private BOFactory() {
    }

    public static BOFactory getInstance() {
        if (boFactory == null) {
            boFactory = new BOFactory();
        }
        return boFactory;
    }

    public SuperBO  getBO(BOType boType) {
        return switch (boType) {
            case PATIENT -> new PatientBOImpl();
            case USER -> new UserBOImpl();
            case THERAPIST -> new TherapistBOImpl();
            case THERAPY_PROGRAM -> new TherapyProgramBOImpl();
            case THERAPY_SESSION -> new TherapySessionBOImpl();
            case PAYMENT -> new PaymentBOImpl();
            default -> null;
        };
    }

    public enum BOType {
        PATIENT, USER, THERAPIST, THERAPY_PROGRAM, THERAPY_SESSION, PAYMENT
    }
}
