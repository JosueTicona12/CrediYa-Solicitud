package co.com.solicitud.WebClient;

import co.com.solicitud.model.solicitud.Usuario;
import co.com.solicitud.model.solicitud.port.UsuarioPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class UsuarioWebClientAdapter implements UsuarioPort {
    private final WebClient usuariosClient;

    public UsuarioWebClientAdapter(@Qualifier("usuariosClient") WebClient usuariosClient) {
        this.usuariosClient = usuariosClient;
    }

    @Override
    public Mono<Usuario> getByDocumento(String documento) {
        return Mono.deferContextual(ctx -> {
            String token = ctx.getOrDefault("authToken", "");
            return usuariosClient.get()
                    .uri(uri -> uri.path("/usuarios/by-documento/{numDocumento}").build(documento))
                    .headers(h -> {
                        if (!token.isBlank()) {
                            h.setBearerAuth(token);
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, r ->
                            r.bodyToMono(String.class).flatMap(msg -> {
                                if (r.statusCode().value() == 404) return Mono.empty();
                                return Mono.error(new RuntimeException("Usuarios 4xx: " + msg));
                            }))
                    .onStatus(HttpStatusCode::is5xxServerError, r ->
                            r.bodyToMono(String.class).flatMap(msg ->
                                    Mono.error(new RuntimeException("Usuarios 5xx: " + msg))))
                    .bodyToMono(Usuario.class);
        });
    }

    @Override
    public Mono<String> getEmailById(Long id) {
        return Mono.deferContextual(ctx -> {
            String token = ctx.getOrDefault("authToken", "");
            return usuariosClient.get()
                    .uri(uri -> uri.path("/usuarios/{id}/email").build(id))
                    .headers(h -> {
                        if (!token.isBlank()) {
                            h.setBearerAuth(token);
                        }
                    })
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
        });
    }
}
