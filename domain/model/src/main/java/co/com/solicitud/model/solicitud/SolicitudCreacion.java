package co.com.solicitud.model.solicitud;

public record SolicitudCreacion(
        String documento,
        Integer monto,
        java.time.LocalDate plazo,
        Long idestado,
        Long idTipoPrestamo,
        boolean solAut
) {

}
