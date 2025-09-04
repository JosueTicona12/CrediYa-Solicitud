package co.com.solicitud.api.config;

import co.com.solicitud.api.Handler;
import co.com.solicitud.api.RouterRest;
import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.usecase.solicitud.SolicitudUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

import static org.mockito.Mockito.when;


@WebFluxTest
@Import({ RouterRest.class, Handler.class, SecurityConfig.class, CorsConfig.class, SecurityHeadersConfig.class, ConfigTest.TestBeans.class })
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SolicitudUseCase solicitudUseCase;


    static final Solicitud U1 = Solicitud.builder()
            .id(1L)
            .monto(1000)
            .plazo(LocalDate.of(2024, 6, 6))
            .documento("73657869")
            .idestado(1L)
            .build();

    @Test
    void corsAndSecurityHeaders_areApplied() {
        when(solicitudUseCase.getAllSolicitud()).thenReturn(Flux.just(U1));

        webTestClient.get()
                .uri("/api/v1/solicitudes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    static class TestBeans {
        @Bean
        public SolicitudPath solicitudPath() {
            var p = new SolicitudPath();
            p.setSolicitudes("/api/v1/solicitudes");
            p.setSolicitudesById("/api/v1/solicitudes/{id}");
            p.setSolicitudesRevision("/api/v1/solicitudes/revision");
            return p;
        }
    }

}