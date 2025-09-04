package co.com.solicitud.model.solicitud;

public record Usuario(
    Long id,
    String nombres,
    String apellidos,
    String email,
    String documento,
    Long activo
    ) {}
