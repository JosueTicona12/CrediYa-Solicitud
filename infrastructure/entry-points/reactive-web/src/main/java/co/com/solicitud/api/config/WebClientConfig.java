package co.com.solicitud.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder
                .filter(this::bearerPropagationFilter)
                .build();
    }

    private Mono<ClientResponse> bearerPropagationFilter(ClientRequest request, ExchangeFunction next) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(Jwt::getTokenValue)
                .defaultIfEmpty("")
                .flatMap(token -> {
                    ClientRequest.Builder mutated = ClientRequest.from(request);
                    if (!token.isBlank()) {
                        mutated.headers(h -> h.setBearerAuth(token));
                    }
                    return next.exchange(mutated.build());
                });
    }
}