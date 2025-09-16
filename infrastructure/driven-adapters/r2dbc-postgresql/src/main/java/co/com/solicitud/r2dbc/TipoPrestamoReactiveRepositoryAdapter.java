package co.com.solicitud.r2dbc;

import co.com.solicitud.model.tipoprestamo.TipoPrestamo;
import co.com.solicitud.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.solicitud.r2dbc.Entity.TipoPrestamoEntity;
import co.com.solicitud.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class TipoPrestamoReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        TipoPrestamo,
        TipoPrestamoEntity,
        Long,
        TipoPrestamoReactiveRepository
        > implements TipoPrestamoRepository {

    public TipoPrestamoReactiveRepositoryAdapter(TipoPrestamoReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, data -> mapper.map(data, TipoPrestamo.class));
    }

    @Override
    public Mono<TipoPrestamo> findById(Long id) {
        return super.findById(id);
    }

}
