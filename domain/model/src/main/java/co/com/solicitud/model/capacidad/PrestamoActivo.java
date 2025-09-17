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
public class PrestamoActivo {
    private BigDecimal monto;
    private BigDecimal tasaInteresMensual;
    private Integer plazoMeses;
}
