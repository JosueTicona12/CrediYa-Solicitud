package co.com.solicitud.usecase.solicitud.utils;

public enum SolicitudStatusEnum {
    SOLICITUD_CREADA("SOLI-001"),
    SOLICITUD_ACTUALIZADA("SOLI-002"),
    SOLICITUD_ELIMINADA("SOLI-003"),
    SOLICITUD_ENCONTRADA("SOLI-004"),
    SOLICITUDES_LISTADAS("SOLI-005"),
    SOLICITUD_NO_ENCONTRADA("SOLI-006"),
    VALIDACION_ERROR("SOLI-007"),
    SOLICITUD_NO_ACTUALIZADA("SOLI-008"),
    SOLICITUD_NO_ELIMINADA("SOLI-009"),
    UNAUTHORIZED("SOLI-010"),
    CAPACIDAD_CALCULADA("CAP-001"),
    CAPACIDAD_VALIDACION_ERROR("CAP-002"),
    ERROR("SOLI-999");

    private final String code;

    SolicitudStatusEnum(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
