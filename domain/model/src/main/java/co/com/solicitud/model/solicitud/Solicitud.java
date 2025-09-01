package co.com.solicitud.model.solicitud;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {

    private Long id;
    private Integer monto;
    private LocalDate plazo;
    private String email;
    private String documento;
    private Long idestado;
    private Long idtipoprestamo;
}
