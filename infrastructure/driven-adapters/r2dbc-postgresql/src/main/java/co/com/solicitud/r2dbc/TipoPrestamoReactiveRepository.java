package co.com.solicitud.r2dbc;

import co.com.solicitud.r2dbc.Entity.TipoPrestamoEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface  TipoPrestamoReactiveRepository extends ReactiveCrudRepository<TipoPrestamoEntity, Long>,
        ReactiveQueryByExampleExecutor<TipoPrestamoEntity> {
}