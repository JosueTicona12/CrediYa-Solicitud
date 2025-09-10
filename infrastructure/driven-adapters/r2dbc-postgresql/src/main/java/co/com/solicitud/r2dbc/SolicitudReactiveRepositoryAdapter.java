package co.com.solicitud.r2dbc;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.gateways.SolicitudRepository;
import co.com.solicitud.r2dbc.Entity.SolicitudEntity;
import co.com.solicitud.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public class SolicitudReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
        String,
        SolicitudReactiveRepository
> implements SolicitudRepository {
    private final TransactionalOperator transactionalOperator;
    public SolicitudReactiveRepositoryAdapter(SolicitudReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Solicitud> save(Solicitud usuario) {
        return transactionalOperator.transactional(super.save(usuario));
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

    @Override
    public Flux<Solicitud> findByIdestadoIn(Collection<Long> estados) {
        return repository.findByIdestadoIn(estados)
                .map(entity -> mapper.map(entity, Solicitud.class));
    }


}
