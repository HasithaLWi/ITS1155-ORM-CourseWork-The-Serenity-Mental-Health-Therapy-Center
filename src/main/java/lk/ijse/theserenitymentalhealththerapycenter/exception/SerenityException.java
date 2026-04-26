package lk.ijse.theserenitymentalhealththerapycenter.exception;

public class SerenityException extends RuntimeException {
    public SerenityException(String message) {
        super(message);
    }

    public SerenityException(String message, Throwable cause) {
        super(message, cause);
    }
}
