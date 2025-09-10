package co.com.solicitud.api;

import co.com.solicitud.api.config.SecurityConfig;
import co.com.solicitud.api.config.SolicitudPath;
import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.SolicitudCreacion;
import co.com.solicitud.usecase.solicitud.SolicitudUseCase;
import co.com.solicitud.usecase.solicitud.utils.SolicitudErrorEnum;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import exceptions.SolicitudNotFoundException;
import exceptions.SolicitudUpdateException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;


import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        RouterRestTest.TestApplication.class,
        RouterRest.class,
        Handler.class,
        SecurityConfig.class,
        RouterRestTest.TestBeans.class
})
@WebFluxTest
class RouterRestTest {

    @MockitoBean
    SolicitudUseCase solicitudUseCase; // lo requiere el Handler

    @Autowired
    WebTestClient webTestClient;

    @SpringBootConfiguration
    static class TestApplication {
    }

    @TestConfiguration
    static class TestBeans {
        @Bean
        SolicitudPath solicitudPath() {
            var p = new SolicitudPath();
            p.setSolicitudes("/api/v1/solicitudes");
            p.setSolicitudesById("/api/v1/solicitudes/{id}");
            p.setSolicitudesRevision("/api/v1/solicitudes/revision");
            return p;
        }
    }

    static final Solicitud U1 = Solicitud.builder()
            .id(1L)
            .monto(1000)
            .plazo(LocalDate.of(2024, 6, 6))
            .documento("73657869")
            .email("juan@test.com")
            .idestado(1L)
            .build();

    static final Solicitud U2 = Solicitud.builder()
            .id(2L)
            .monto(1500)
            .plazo(LocalDate.of(2024, 6, 6))
            .documento("12345898")
            .email("maria@test.com")
            .idestado(1L)
            .build();


    static final String CLIENT_TOKEN = "Bearer " +
            JWT.create().withClaim("rol", "3").sign(Algorithm.HMAC256("secret"));
    static final String ASESOR_TOKEN = "Bearer " +
            JWT.create().withClaim("rol", "2").sign(Algorithm.HMAC256("secret"));

    @Test
    void getAllUsuarios_ok() {
        when(solicitudUseCase.getAllSolicitud()).thenReturn(Flux.just(U1, U2));

        webTestClient.get()
                .uri("/api/v1/solicitudes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Solicitud.class)
                .hasSize(2)
                .value(list -> Assertions.assertThat(list.get(0).getId()).isEqualTo(1L));
    }

    @Test
    void getUsuarioById_ok() {
        when(solicitudUseCase.getSolicitudById(1L)).thenReturn(Mono.just(U1));

        webTestClient.get()
                .uri("/api/v1/solicitudes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Solicitud.class)
                .value(u -> Assertions.assertThat(u.getEmail()).isEqualTo("juan@test.com"));
    }

    @Test
    void postSaveUsuario_ok() {
        var dto = new SolicitudCreacion(U1.getDocumento(), U1.getMonto(), U1.getPlazo(), U1.getIdestado(), 1L);
        when(solicitudUseCase.crearSolicitud(ArgumentMatchers.any(SolicitudCreacion.class), ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(U1));

        String token = JWT.create()
                .withSubject(U1.getEmail())
                .withClaim("rol", "3")
                .sign(Algorithm.HMAC256("secret"));


        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Solicitud.class)
                .value(u -> Assertions.assertThat(u.getId()).isEqualTo(1L));

    }

    @Test
    void putUpdateUsuario_ok() {
        Solicitud cambios = U1.toBuilder().monto(500).plazo(LocalDate.ofEpochDay(1 - 2 - 2000)).build();
        when(solicitudUseCase.updateSolicitud(ArgumentMatchers.any(Solicitud.class), ArgumentMatchers.eq(1L)))
                .thenReturn(Mono.just(cambios));

        webTestClient.put()
                .uri("/api/v1/solicitudes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cambios)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.message").value(msg -> Assertions.assertThat(msg).asString().isNotBlank());
    }

    @Test
    void deleteUsuario_noContent() {
        when(solicitudUseCase.deleteSolicitud(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/solicitudes/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getSolicitudesRevision_ok() {
        when(solicitudUseCase.getSolicitudesRevision(0, 10, "", List.of()))
                .thenReturn(Flux.just(U1));

        webTestClient.get()
                .uri("/api/v1/solicitudes/revision?page=0&size=10")
                .header(HttpHeaders.AUTHORIZATION, ASESOR_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Solicitud.class)
                .hasSize(1);
    }

    @Test
    void postSaveUsuario_unauthorizedWhenNotClient() {
        var dto = new SolicitudCreacion(U1.getDocumento(), U1.getMonto(), U1.getPlazo(), U1.getIdestado(), 1L);

        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .header(HttpHeaders.AUTHORIZATION, ASESOR_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo(SolicitudErrorEnum.TOKEN_INVALIDO_CLIENTE.message());
    }

    @Test
    void postSaveUsuario_errorFromUseCase() {
        var dto = new SolicitudCreacion(U1.getDocumento(), U1.getMonto(), U1.getPlazo(), U1.getIdestado(), 1L);
        String token = JWT.create().withSubject(U1.getEmail()).withClaim("rol", "3").sign(Algorithm.HMAC256("secret"));
        when(solicitudUseCase.crearSolicitud(ArgumentMatchers.any(SolicitudCreacion.class), ArgumentMatchers.anyString()))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("boom");
    }

    @Test
    void putUpdateUsuario_notFound() {
        when(solicitudUseCase.updateSolicitud(ArgumentMatchers.any(Solicitud.class), ArgumentMatchers.eq(1L)))
                .thenReturn(Mono.error(new SolicitudNotFoundException(1L)));

        webTestClient.put()
                .uri("/api/v1/solicitudes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(U1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void putUpdateUsuario_conflict() {
        when(solicitudUseCase.updateSolicitud(ArgumentMatchers.any(Solicitud.class), ArgumentMatchers.eq(1L)))
                .thenReturn(Mono.error(new SolicitudUpdateException(1L)));

        webTestClient.put()
                .uri("/api/v1/solicitudes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(U1)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void getSolicitudesRevision_unauthorized() {
        webTestClient.get()
                .uri("/api/v1/solicitudes/revision?page=0&size=10")
                .header(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo(SolicitudErrorEnum.TOKEN_INVALIDO_ASESOR.message());
    }
}
