package co.com.solicitud.model.solicitud.dto;

public record UsuarioDTO (
    Long id,
    String nombres,
    String apellidos,
    String email,
    String documento,
    Long activo
    ) {}
