package co.com.solicitud.usecase.solicitud;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.dto.SolicitudCreacionDTO;
import co.com.solicitud.model.solicitud.dto.UsuarioDTO;
import co.com.solicitud.model.solicitud.gateways.SolicitudRepository;
import co.com.solicitud.model.solicitud.port.UsuarioPort;
import exceptions.SolicitudException;
import exceptions.SolicitudNotFoundException;
import exceptions.SolicitudValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudTest {
    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioPort usuarioPort;

    @InjectMocks
    private SolicitudUseCase useCase;

    private static SolicitudCreacionDTO buildOkDTO() {
        return new SolicitudCreacionDTO(
                "73657869",
                10_000,
                LocalDate.now().plusDays(5),
                1L,
                1L
        );
    }

    @Test
    void crearSolicitud_ok() {
        // Arrange
        var dto = buildOkDTO();
        var usuario = new UsuarioDTO(1L, "Josue", "Ticona", "user@test.com", 1L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuario));
        when(solicitudRepository.findByEmail(usuario.email())).thenReturn(Mono.empty());
        when(solicitudRepository.save(any(Solicitud.class)))
                .thenAnswer(inv -> Mono.just((Solicitud) inv.getArgument(0)));

        // Act & Assert
        StepVerifier.create(useCase.crearSolicitud(dto))
                .expectNextMatches(s -> s.getEmail().equals("user@test.com")
                        && s.getMonto().equals(10_000)
                        && s.getPlazo().isAfter(LocalDate.now())
                        && s.getIdestado().equals(1L)
                        && s.getIdtipoprestamo().equals(1L))
                .verifyComplete();

        verify(usuarioPort).getByDocumento(dto.documento());
        verify(solicitudRepository).findByEmail("user@test.com");
        verify(solicitudRepository).save(any(Solicitud.class));
        verifyNoMoreInteractions(usuarioPort, solicitudRepository);
    }

    @Test
    void crearSolicitud_errorDocumentoObligatorio() {
        var dto = new SolicitudCreacionDTO(
                "  ", 10_000, LocalDate.now().plusDays(1), 2L, 1L
        );

        StepVerifier.create(useCase.crearSolicitud(dto))
                .expectError(SolicitudValidationException.class)
                .verify();

        verifyNoInteractions(usuarioPort, solicitudRepository);
    }

    @Test
    void crearSolicitud_errorMontoInvalido() {
        var dto = new SolicitudCreacionDTO(
                "73657869", 0, LocalDate.now().plusDays(1), 2L, 1L
        );

        StepVerifier.create(useCase.crearSolicitud(dto))
                .expectError(SolicitudValidationException.class)
                .verify();

        verifyNoInteractions(usuarioPort, solicitudRepository);
    }

    @Test
    void crearSolicitud_errorPlazoNoFuturo() {
        var dto = new SolicitudCreacionDTO(
                "73657869", 10_000, LocalDate.now(), 2L, 1L
        );

        StepVerifier.create(useCase.crearSolicitud(dto))
                .expectError(SolicitudValidationException.class)
                .verify();

        verifyNoInteractions(usuarioPort, solicitudRepository);
    }

    @Test
    void crearSolicitud_errorUsuarioNoEncontrado() {
        var dto = buildOkDTO();

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.crearSolicitud(dto))
                .expectError(SolicitudNotFoundException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verifyNoMoreInteractions(usuarioPort);
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crearSolicitud_errorUsuarioSinEmail() {
        var dto = buildOkDTO();
        var usuarioSinEmail = new UsuarioDTO(2L, "  ", " ", "", 1L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuarioSinEmail));

        StepVerifier.create(useCase.crearSolicitud(dto))
                .expectError(SolicitudValidationException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crearSolicitud_errorEmailDuplicadoEnSolicitudes() {
        var dto = buildOkDTO();
        var usuario = new UsuarioDTO(7L, "Josue", "ticona", "taken@test.com", 1L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuario));
        when(solicitudRepository.findByEmail("taken@test.com"))
                .thenReturn(Mono.just(new Solicitud()));

        StepVerifier.create(useCase.crearSolicitud(dto))
                .expectError(SolicitudException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verify(solicitudRepository).findByEmail("taken@test.com");
        verifyNoMoreInteractions(usuarioPort, solicitudRepository);
    }
}
