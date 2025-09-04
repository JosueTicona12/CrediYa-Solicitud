package exceptions;

import co.com.solicitud.usecase.solicitud.utils.SolicitudErrorEnum;

public class SolicitudDeleteException extends RuntimeException {
    public SolicitudDeleteException(Long id) {
        super(String.format(SolicitudErrorEnum.NO_SE_PUDO_ELIMINAR.message(), id));

    }
}


