package co.com.solicitud.model.capacidad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacidadEndeudamientoRequest {
    private BigDecimal ingresosTotales;
    private List<PrestamoActivo> prestamosActivos;
    private BigDecimal montoNuevo;
    private BigDecimal tasaInteresMensualNueva;
    private Integer plazoMesesNuevo;
}
