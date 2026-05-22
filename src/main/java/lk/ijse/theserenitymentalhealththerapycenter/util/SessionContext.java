package lk.ijse.theserenitymentalhealththerapycenter.util;

import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;



public class SessionContext {
    private static UserDTO currentUser;

    public static UserDTO getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserDTO user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
