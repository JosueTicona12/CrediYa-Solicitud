package co.com.solicitud.api;

import co.com.solicitud.api.config.SolicitudPath;
import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.dto.SolicitudCreacionDTO;
import co.com.solicitud.usecase.solicitud.SolicitudUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
class RouterRestTest {

    @MockitoBean
    SolicitudUseCase solicitudUseCase; // lo requiere el Handler

    @Autowired
    WebTestClient webTestClient;

    static class TestBeans {
        @Bean
        SolicitudPath solicitudPath() {
            var p = new SolicitudPath();
            p.setSolicitudes("/api/v1/solicitudes");
            p.setSolicitudesById("/api/v1/solicitudes/{id}");
            return p;
        }
    }

    static final Solicitud U1 = Solicitud.builder()
            .id(1L)
            .monto(1000)
            .plazo(LocalDate.of(2024, 6, 6))
            .documento("73657869")
            .idestado(1L)
            .build();

    static final Solicitud U2 = Solicitud.builder()
            .id(2L)
            .monto(1500)
            .plazo(LocalDate.of(2024, 6, 6))
            .documento("12345898")
            .idestado(1L)
            .build();


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
        when(solicitudUseCase.crearSolicitud(ArgumentMatchers.any(SolicitudCreacionDTO.class))).thenReturn(Mono.just(U1));

        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(U1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(200) // o el que uses en tu SuccessResponse
                .jsonPath("$.message").isNotEmpty();
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
}
