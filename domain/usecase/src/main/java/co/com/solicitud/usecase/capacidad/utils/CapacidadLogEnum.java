package co.com.solicitud.usecase.capacidad.utils;

public enum CapacidadLogEnum {
    CALCULO_INICIADO("UseCase - Iniciando cálculo de capacidad de endeudamiento"),
    CALCULO_COMPLETADO("UseCase - Cálculo de capacidad de endeudamiento completado"),
    ERROR_CALCULO("UseCase - Error calculando capacidad de endeudamiento: "),
    PETICION_CALCULO("Handler - Recibida petición de cálculo de capacidad de endeudamiento");

    private final String message;

    CapacidadLogEnum(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
