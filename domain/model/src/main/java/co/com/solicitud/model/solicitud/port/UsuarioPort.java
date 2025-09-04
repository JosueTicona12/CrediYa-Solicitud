package co.com.solicitud.model.solicitud.port;

import co.com.solicitud.model.solicitud.Usuario;
import reactor.core.publisher.Mono;

public interface UsuarioPort {
    Mono<Usuario> getByDocumento(String documento);
    Mono<String> getEmailById(Long id);

}