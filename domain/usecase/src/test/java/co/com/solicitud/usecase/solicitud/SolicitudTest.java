package co.com.solicitud.usecase.solicitud;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.gateways.SolicitudRepository;
import exceptions.SolicitudException;
import exceptions.SolicitudNotFoundException;
import exceptions.SolicitudValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudTest {
    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private SolicitudUseCase solicitudUseCase;

    private Solicitud solicitud;

    @BeforeEach
    void setUp() {
        solicitud = new Solicitud();
        solicitud.setId(1L);
        solicitud.setMonto(5000);
        solicitud.setPlazo(LocalDate.now().plusMonths(6));
        solicitud.setEmail("test@correo.com");
        solicitud.setIdestado(1L);
        solicitud.setIdtipoprestamo(2L);
    }

    @Test
    void saveServicio_success() {
        when(solicitudRepository.findByEmail(solicitud.getEmail())).thenReturn(Mono.empty());
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(solicitud));

        StepVerifier.create(solicitudUseCase.saveServicio(solicitud))
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    void saveServicio_invalidEmail() {
        solicitud.setEmail(" ");

        StepVerifier.create(solicitudUseCase.saveServicio(solicitud))
                .expectError(SolicitudValidationException.class)
                .verify();
    }

    @Test
    void updateServicio_success() {
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(solicitud));

        StepVerifier.create(solicitudUseCase.updateServicio(solicitud, 1L))
                .expectNextMatches(s -> s.getMonto().equals(5000))
                .verifyComplete();
    }

    @Test
    void updateServicio_notFound() {
        when(solicitudRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.updateServicio(solicitud, 1L))
                .expectError(SolicitudNotFoundException.class)
                .verify();
    }

    @Test
    void getAllServicio_success() {
        when(solicitudRepository.findAll()).thenReturn(Flux.just(solicitud));

        StepVerifier.create(solicitudUseCase.getAllServicio())
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    void getAllServicio_empty() {
        when(solicitudRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(solicitudUseCase.getAllServicio())
                .expectError(SolicitudException.class)
                .verify();
    }

    @Test
    void getServicioById_success() {
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(solicitud));

        StepVerifier.create(solicitudUseCase.getServicioById(1L))
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    void getServicioById_notFound() {
        when(solicitudRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.getServicioById(1L))
                .expectError(SolicitudNotFoundException.class)
                .verify();
    }

    @Test
    void deleteServicio_success() {
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(solicitud));
        when(solicitudRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.deleteServicio(1L))
                .verifyComplete();
    }

    @Test
    void findByEmail_success() {
        when(solicitudRepository.findByEmail("test@correo.com")).thenReturn(Mono.just(solicitud));

        StepVerifier.create(solicitudUseCase.findByEmail("test@correo.com"))
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    void findByEmail_notFound() {
        when(solicitudRepository.findByEmail("noexiste@correo.com")).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.findByEmail("noexiste@correo.com"))
                .expectError(SolicitudException.class)
                .verify();
    }
}
