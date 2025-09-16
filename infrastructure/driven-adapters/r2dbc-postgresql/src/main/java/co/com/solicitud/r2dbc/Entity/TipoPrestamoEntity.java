package co.com.solicitud.r2dbc.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table("tipo_prestamo")
public class TipoPrestamoEntity {
    @Id
    @Column("id")
    private Long id;

    private String nombre;

    @Column("validacionAutomatica")
    private Boolean solAut;
}
