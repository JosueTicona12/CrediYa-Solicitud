package co.com.solicitud.api.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
public class SuccessResponse {
    private LocalDateTime timestamp;
    private String status;
    private String message;
}
