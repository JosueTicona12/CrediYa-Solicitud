package co.com.solicitud.usecase.solicitud.utils;

public enum SolicitudErrorEnum {
    NO_SOLICITUDES_REVISION("No se encontraron solicitudes para revisión"),
    DOCUMENTO_OBLIGATORIO("El documento es obligatorio"),
    MONTO_INVALIDO("El monto debe ser > 0"),
    PLAZO_INVALIDO("El plazo debe ser una fecha futura"),
    USUARIO_INACTIVO("El usuario está inactivo"),
    USUARIO_EMAIL_INVALIDO("El usuario no tiene email válido"),
    TOKEN_NO_PERTENECE("El token no pertenece al usuario"),
    SOLICITUD_EXISTE("Ya existe una solicitud registrada con el email: %s"),
    ID_NULO("El id no puede ser nulo"),
    SOLICITUD_NULA("La solicitud no puede ser nula"),
    NO_SOLICITUDES("No se encontraron solicitudes"),
    SOLICITUD_NO_ENCONTRADA("Servicio con id %s no encontrado"),
    NO_SE_PUDO_ELIMINAR("No se pudo eliminar el servicio con id %s"),
    NO_SE_PUDO_ACTUALIZAR("No se pudo actualizar el servicio con id %s"),
    ERROR_VALIDACION("Error de validación: %s"),
    EMAIL_VACIO("El campo email esta vacio %s"),
    TOKEN_INVALIDO_CLIENTE("Token inválido: se requiere ser CLIENTE"),
    TOKEN_INVALIDO_ASESOR("Token inválido: se requiere ser ASESOR");

    private final String message;

    SolicitudErrorEnum(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
