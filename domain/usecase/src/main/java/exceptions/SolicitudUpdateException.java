package exceptions;

import co.com.solicitud.usecase.solicitud.utils.SolicitudErrorEnum;

public class SolicitudUpdateException extends RuntimeException {
    public SolicitudUpdateException(Long id) {
        super(String.format(SolicitudErrorEnum.NO_SE_PUDO_ACTUALIZAR.message(), id));
    }
}
