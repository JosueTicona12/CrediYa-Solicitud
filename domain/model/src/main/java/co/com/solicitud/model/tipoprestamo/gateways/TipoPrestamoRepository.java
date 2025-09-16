package co.com.solicitud.model.tipoprestamo.gateways;

import co.com.solicitud.model.tipoprestamo.TipoPrestamo;
import reactor.core.publisher.Mono;

public interface TipoPrestamoRepository {
    Mono<TipoPrestamo> findById(Long id);

}
