package lk.ijse.theserenitymentalhealththerapycenter.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OtpManager {
    private static final SecureRandom random = new SecureRandom();
    private static final Map<String, OtpDetails> otpCache = new ConcurrentHashMap<>();
    private static volatile String activeRegistrationCode = null;

    private static class OtpDetails {
        String code;
        LocalDateTime expiryTime;

        OtpDetails(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    public static String generateOtp(String username) {
        int codeInt = 100000 + random.nextInt(900000);
        String code = String.valueOf(codeInt);
        otpCache.put(username, new OtpDetails(code, LocalDateTime.now().plusMinutes(5)));
        return code;
    }

    public static boolean validateOtp(String username, String enteredCode) {
        OtpDetails details = otpCache.get(username);
        if (details == null) {
            return false;
        }
        if (details.isExpired()) {
            otpCache.remove(username);
            return false;
        }
        boolean isValid = details.code.equals(enteredCode);
        if (isValid) {
            otpCache.remove(username);
        }
        return isValid;
    }

    public static String generateRegistrationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();
        activeRegistrationCode = code;
        return code;
    }

    public static boolean checkRegistrationCode(String enteredCode) {
        if (activeRegistrationCode == null || enteredCode == null) {
            return false;
        }
        return activeRegistrationCode.equalsIgnoreCase(enteredCode.trim());
    }

    public static boolean consumeRegistrationCode(String enteredCode) {
        if (activeRegistrationCode == null || enteredCode == null) {
            return false;
        }
        boolean isValid = activeRegistrationCode.equalsIgnoreCase(enteredCode.trim());
        if (isValid) {
            activeRegistrationCode = null;
        }
        return isValid;
    }
}
