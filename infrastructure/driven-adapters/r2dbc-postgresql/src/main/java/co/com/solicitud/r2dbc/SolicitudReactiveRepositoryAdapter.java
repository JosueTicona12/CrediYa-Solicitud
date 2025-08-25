package co.com.solicitud.r2dbc;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.gateways.SolicitudRepository;
import co.com.solicitud.r2dbc.Entity.SolicitudEntity;
import co.com.solicitud.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SolicitudReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
        String,
        SolicitudReactiveRepository
> implements SolicitudRepository {
    public SolicitudReactiveRepositoryAdapter(SolicitudReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
    }

    @Override
    public Mono<Solicitud> save(Solicitud usuario) {
        return super.save(usuario);
    }

    @Override
    public Flux<Solicitud> findAll() {
        return super.findAll();
    }

    @Override
    public Mono<Solicitud> findById(Long id) {
        return super.findById(String.valueOf(id));
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return super.repository.deleteById(String.valueOf(id));
    }

    @Override
    public Mono<Solicitud> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(entity -> mapper.map(entity, Solicitud.class));
    }
}
