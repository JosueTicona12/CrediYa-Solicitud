package co.com.solicitud.model.solicitud.port;

import reactor.core.publisher.Mono;

public interface NotificacionPort {
    Mono<Void> enviarNotificacion(String email, String estado);
}
