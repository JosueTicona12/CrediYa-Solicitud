package co.com.solicitud.r2dbc.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
@Table("solicitud")
public class SolicitudEntity {
    @Id
    @Column("id")
    private Long id;
    private Integer monto;
    private LocalDate plazo;
    private String email;
    private Long idtipoprestamo;
    private Long idestado;
}
