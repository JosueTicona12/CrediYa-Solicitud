package co.com.solicitud.r2dbc.WebClient;

import co.com.solicitud.model.solicitud.dto.UsuarioDTO;
import co.com.solicitud.model.solicitud.port.UsuarioPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UsuarioWebClientAdapter implements UsuarioPort {
    private final WebClient webClient;

    @Override
    public Mono<UsuarioDTO> getByDocumento(String documento) {
        return webClient.get()
                .uri(uri -> uri.path("/usuarios/by-documento/{numDocumento}").build(documento))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class).flatMap(msg -> {
                            if (r.statusCode().value() == 404) return Mono.empty();
                            return Mono.error(new RuntimeException("Usuarios 4xx: " + msg));
                        }))
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                      r.bodyToMono(String.class).flatMap(msg ->
                             Mono.error(new RuntimeException("Usuarios 5xx: " + msg))))
                .bodyToMono(UsuarioDTO.class);
    }

    @Override
    public Mono<String> getEmailById(Long id) {
        return webClient.get()
                .uri(uri -> uri.path("/usuarios/{id}/email").build(id))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class).flatMap(msg -> {
                            if (r.statusCode().value() == 404) return Mono.empty();
                            return Mono.error(new RuntimeException("Usuarios 4xx: " + msg));
                        }))
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                        r.bodyToMono(String.class).flatMap(msg ->
                                Mono.error(new RuntimeException("Usuarios 5xx: " + msg))))
                .bodyToMono(String.class);
    }
}
