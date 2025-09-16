package co.com.solicitud.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "routes.paths")
public class SolicitudPath {
    private String solicitudes;
    private String solicitudesById;
    private String solicitudesRevision;
    private String calcularCapacidad;
}
