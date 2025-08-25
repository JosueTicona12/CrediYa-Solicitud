package exceptions;

public class SolicitudNotFoundException extends RuntimeException {
    public SolicitudNotFoundException(Long id) {
        super("Servicio con id " + id + " no encontrado");
    }
}
