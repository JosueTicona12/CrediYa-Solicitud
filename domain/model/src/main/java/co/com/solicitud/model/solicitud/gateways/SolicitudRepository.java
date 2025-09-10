package co.com.solicitud.model.solicitud.gateways;

import co.com.solicitud.model.solicitud.Solicitud;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface SolicitudRepository {

    Mono<Solicitud> save(Solicitud solicitud);
    Flux<Solicitud> findAll();
    Mono<Solicitud> findById(Long id);
    Mono<Void> deleteById(Long id);
    Mono<Solicitud> findByEmail(String email);
    Flux<Solicitud> findByIdestadoIn(Collection<Long> estados);
}
