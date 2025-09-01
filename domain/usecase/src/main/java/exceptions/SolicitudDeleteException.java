package exceptions;

public class SolicitudDeleteException extends RuntimeException {
    public SolicitudDeleteException(Long id) {
        super("No se pudo eliminar el servicio con id " + id);
    }
}


