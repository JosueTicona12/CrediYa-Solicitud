package exceptions;

import co.com.solicitud.usecase.solicitud.utils.SolicitudErrorEnum;

public class SolicitudValidationException extends RuntimeException {
    public SolicitudValidationException(String message) {
        super(String.format(SolicitudErrorEnum.ERROR_VALIDACION.message(), message));
    }
}
