package lk.ijse.theserenitymentalhealththerapycenter.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {


    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }


    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
