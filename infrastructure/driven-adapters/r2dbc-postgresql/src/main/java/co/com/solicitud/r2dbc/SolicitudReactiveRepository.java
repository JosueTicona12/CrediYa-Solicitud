package co.com.solicitud.r2dbc;

import co.com.solicitud.r2dbc.Entity.SolicitudEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

// TODO: This file is just an example, you should delete or modify it
public interface SolicitudReactiveRepository extends ReactiveCrudRepository<SolicitudEntity, String>, ReactiveQueryByExampleExecutor<SolicitudEntity> {

    Mono<SolicitudEntity> findByEmail(String email);
    Flux<SolicitudEntity> findByIdestadoIn(Collection<Long> estados);

}
