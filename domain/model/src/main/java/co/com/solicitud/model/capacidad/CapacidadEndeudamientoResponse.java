package co.com.solicitud.model.capacidad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacidadEndeudamientoResponse {
    private BigDecimal capacidadEndeudamientoMaxima;
    private BigDecimal deudaMensualActual;
    private BigDecimal capacidadEndeudamientoDisponible;
    private BigDecimal cuotaPrestamoNuevo;
    private boolean aprobado;
    private String decision;
}
