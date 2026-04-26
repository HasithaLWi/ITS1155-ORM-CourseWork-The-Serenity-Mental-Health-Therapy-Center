package lk.ijse.theserenitymentalhealththerapycenter.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^(\\+94|0)?[0-9]{9,10}$";
    private static final String NIC_REGEX = "^([0-9]{9}[vVxX]|[0-9]{12})$";

    public static boolean isValidEmail(String email) {
        return email != null && Pattern.matches(EMAIL_REGEX, email.trim());
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && Pattern.matches(PHONE_REGEX, phone.trim());
    }

    public static boolean isValidNIC(String nic) {
        return nic != null && Pattern.matches(NIC_REGEX, nic.trim());
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.trim().matches("^[A-Za-z .'-]+$");
    }
}
