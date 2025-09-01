package exceptions;

public class SolicitudValidationException extends RuntimeException {
    public SolicitudValidationException(String message) {
        super("Error de validación: " + message);
    }
}
