package co.com.solicitud.model.solicitud.port;

import co.com.solicitud.model.solicitud.dto.UsuarioDTO;
import reactor.core.publisher.Mono;

public interface UsuarioPort {
    Mono<UsuarioDTO> getByDocumento(String documento);
    Mono<String> getEmailById(Long id);

}