package exceptions;

import co.com.solicitud.usecase.solicitud.utils.SolicitudErrorEnum;

public class SolicitudNotFoundException extends RuntimeException {
    public SolicitudNotFoundException(Long id) {
        super(String.format(SolicitudErrorEnum.SOLICITUD_NO_ENCONTRADA.message(), id));
    }
    public SolicitudNotFoundException(String id) {
        super(String.format(SolicitudErrorEnum.SOLICITUD_NO_ENCONTRADA.message(), id));
    }
}
