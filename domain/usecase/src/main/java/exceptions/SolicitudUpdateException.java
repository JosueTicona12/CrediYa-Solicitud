package exceptions;

public class SolicitudUpdateException extends RuntimeException {
    public SolicitudUpdateException(Long id) {
        super("No se pudo actualizar el servicio con id " + id);
    }
}
