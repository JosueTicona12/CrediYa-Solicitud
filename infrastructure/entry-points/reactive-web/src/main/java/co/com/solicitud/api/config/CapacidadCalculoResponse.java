package co.com.solicitud.api.config;

import co.com.solicitud.model.capacidad.CapacidadEndeudamientoResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CapacidadCalculoResponse {
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();
    private String status;
    private String message;
    private CapacidadEndeudamientoResponse data;
}
