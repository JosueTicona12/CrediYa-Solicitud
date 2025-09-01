package co.com.solicitud.model.solicitud.dto;

public record SolicitudCreacionDTO (
        String documento,
        Integer monto,
        java.time.LocalDate plazo,
        Long idestado,
        Long idTipoPrestamo) {

}
