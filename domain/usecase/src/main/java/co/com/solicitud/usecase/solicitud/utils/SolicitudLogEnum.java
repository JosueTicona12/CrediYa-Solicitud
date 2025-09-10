package co.com.solicitud.usecase.solicitud.utils;

public enum SolicitudLogEnum {
    OBTENER_SOLICITUDES_REVISION("UseCase - Obtener solicitudes para revisión"),
    ACTUALIZAR_SOLICITUD("UseCase - Actualizando solicitud con id "),
    SOLICITUD_ACTUALIZADA("Solicitud actualizada: "),
    ERROR_ACTUALIZAR_SOLICITUD("Error actualizando solicitud con id "),
    BUSCAR_TODAS_SOLICITUDES("UseCase - Buscar todas las solicitudes"),
    CONSULTA_COMPLETADA("Consulta de solicitudes completada"),
    ERROR_CONSULTA_SOLICITUDES("Error consultando todas las solicitudes: "),
    BUSCAR_SOLICITUD_POR_ID("UseCase - Buscar solicitud por id "),
    SOLICITUD_ENCONTRADA("Solicitud encontrada: "),
    ERROR_SOLICITUD_POR_ID("Error buscando solicitud con id "),
    ELIMINAR_SOLICITUD("UseCase - Eliminando solicitud con id "),
    SOLICITUD_ELIMINADA("Solicitud eliminado con id "),
    ERROR_ELIMINAR_SOLICITUD("Error eliminando solicitud con id "),
    BUSCAR_POR_EMAIL("UseCase - Busqueda de solicitud por correo"),
    USUARIO_ENCONTRADO("Usuario encontrado: "),
    ERROR_BUSCAR_POR_EMAIL("Error buscando usuario con email "),
    PETICION_ACTUALIZACION("Handler - Recibida petición de actualización para solicitud con id "),
    PETICION_OBTENER_TODAS("Handler - Recibida petición de obtener todas las solicitudes"),
    PETICION_REVISION("Handler - Recibida petición de obtener solicitudes para revisión"),
    PETICION_OBTENER_ID("Handler - Recibida petición de obtener solicitud con id "),
    PETICION_ELIMINAR("Handler - Recibida petición de eliminar solicitud con id ");
    private final String message;

    SolicitudLogEnum(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
