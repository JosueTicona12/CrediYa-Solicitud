package co.com.solicitud.usecase.solicitud;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.SolicitudCreacion;
import co.com.solicitud.model.solicitud.Usuario;
import co.com.solicitud.model.solicitud.gateways.SolicitudRepository;
import co.com.solicitud.model.solicitud.port.NotificacionPort;
import co.com.solicitud.model.solicitud.port.UsuarioPort;
import exceptions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioPort usuarioPort;

    @Mock
    private NotificacionPort notificacionPort;

    @InjectMocks
    private SolicitudUseCase useCase;

    private static SolicitudCreacion buildOkDTO() {
        return new SolicitudCreacion(
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
        var usuario = new Usuario(1L, "Josue", "Ticona", "user@test.com", "",1L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuario));
        when(solicitudRepository.findByEmail(usuario.email())).thenReturn(Mono.empty());
        when(solicitudRepository.save(any(Solicitud.class)))
                .thenAnswer(inv -> Mono.just((Solicitud) inv.getArgument(0)));

        // Act & Assert
        StepVerifier.create(useCase.crearSolicitud(dto, "user@test.com"))
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
        var dto = new SolicitudCreacion(
                "  ", 10_000, LocalDate.now().plusDays(1), 2L, 1L
        );

        StepVerifier.create(useCase.crearSolicitud(dto, "token@test.com"))
                .expectError(SolicitudValidationException.class)
                .verify();

        verifyNoInteractions(usuarioPort, solicitudRepository);
    }

    @Test
    void crearSolicitud_errorMontoInvalido() {
        var dto = new SolicitudCreacion(
                "73657869", 0, LocalDate.now().plusDays(1), 2L, 1L
        );

        StepVerifier.create(useCase.crearSolicitud(dto, "token@test.com"))
                .expectError(SolicitudValidationException.class)
                .verify();

        verifyNoInteractions(usuarioPort, solicitudRepository);
    }

    @Test
    void crearSolicitud_errorPlazoNoFuturo() {
        var dto = new SolicitudCreacion(
                "73657869", 10_000, LocalDate.now(), 2L, 1L
        );

        StepVerifier.create(useCase.crearSolicitud(dto, "token@test.com"))
                .expectError(SolicitudValidationException.class)
                .verify();

        verifyNoInteractions(usuarioPort, solicitudRepository);
    }

    @Test
    void crearSolicitud_errorUsuarioNoEncontrado() {
        var dto = buildOkDTO();

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.crearSolicitud(dto, "token@test.com"))
                .expectError(SolicitudNotFoundException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verifyNoMoreInteractions(usuarioPort);
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crearSolicitud_errorUsuarioSinEmail() {
        var dto = buildOkDTO();
        var usuarioSinEmail = new Usuario(2L, "  ", " ", "", "",1L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuarioSinEmail));

        StepVerifier.create(useCase.crearSolicitud(dto, "token@test.com"))
                .expectError(SolicitudValidationException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crearSolicitud_errorEmailDuplicadoEnSolicitudes() {
        var dto = buildOkDTO();
        var usuario = new Usuario(7L, "Josue", "ticona", "taken@test.com", "", 1L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuario));
        when(solicitudRepository.findByEmail("taken@test.com"))
                .thenReturn(Mono.just(new Solicitud()));

        StepVerifier.create(useCase.crearSolicitud(dto, "taken@test.com"))
                .expectError(SolicitudException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verify(solicitudRepository).findByEmail("taken@test.com");
        verifyNoMoreInteractions(usuarioPort, solicitudRepository);
    }
    @Test
    void crearSolicitud_errorTokenNoPertenece() {
        var dto = buildOkDTO();
        var usuario = new Usuario(1L, "Josue", "Ticona", "user@test.com", "", 1L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuario));

        StepVerifier.create(useCase.crearSolicitud(dto, "otro@test.com"))
                .expectError(SolicitudException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void getSolicitudesRevision_ok() {
        var s1 = new Solicitud();
        s1.setEmail("asesor@test.com");
        s1.setIdestado(2L);

        when(solicitudRepository.findByIdestadoIn(List.of(2L)))
                .thenReturn(Flux.just(s1));

        StepVerifier.create(useCase.getSolicitudesRevision(0, 10, "asesor", List.of(2L)))
                .expectNext(s1)
                .verifyComplete();

        verify(solicitudRepository).findByIdestadoIn(List.of(2L));
    }
    @Test
    void crearSolicitud_errorTokenNulo() {
        var dto = buildOkDTO();
        var usuario = new Usuario(1L, "Josue", "Ticona", "user@test.com", "", 1L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuario));

        StepVerifier.create(useCase.crearSolicitud(dto, null))
                .expectError(SolicitudException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crearSolicitud_errorUsuarioInactivo() {
        var dto = buildOkDTO();
        var usuarioInactivo = new Usuario(1L, "Josue", "Ticona", "user@test.com", "", 0L);

        when(usuarioPort.getByDocumento(dto.documento())).thenReturn(Mono.just(usuarioInactivo));

        StepVerifier.create(useCase.crearSolicitud(dto, "user@test.com"))
                .expectError(SolicitudException.class)
                .verify();

        verify(usuarioPort).getByDocumento(dto.documento());
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void updateSolicitud_ok() {
        var input = new Solicitud();
        input.setMonto(200);
        input.setEmail("new@test.com");
        input.setPlazo(LocalDate.now().plusDays(2));
        input.setIdestado(2L);
        input.setIdtipoprestamo(3L);

        var existing = new Solicitud();
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.updateSolicitud(input, 1L))
                .expectNextMatches(s -> s.getMonto().equals(200) && s.getEmail().equals("new@test.com")
                        && s.getPlazo().equals(input.getPlazo())
                        && s.getIdestado().equals(2L) && s.getIdtipoprestamo().equals(3L))
                .verifyComplete();

        verify(solicitudRepository).findById(1L);
        verify(solicitudRepository).save(any(Solicitud.class));
    }
    @Test
    void updateSolicitud_enviaNotificacionCuandoEstadoFinal() {
        var solicitud = new Solicitud();
        solicitud.setIdestado(3L);
        solicitud.setEmail("final@test.com");
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(new Solicitud()));
        when(solicitudRepository.save(any(Solicitud.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.updateSolicitud(solicitud, 1L))
                .expectNextMatches(s -> s.getIdestado().equals(3L))
                .verifyComplete();

        verify(solicitudRepository).findById(1L);
        verify(solicitudRepository).save(any(Solicitud.class));
        verify(notificacionPort).enviarNotificacion("final@test.com", "Aprobado");
    }


    @Test
    void updateSolicitud_errorIdNulo() {
        StepVerifier.create(useCase.updateSolicitud(new Solicitud(), null))
                .expectError(SolicitudValidationException.class)
                .verify();
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void updateSolicitud_errorSolicitudNula() {
        StepVerifier.create(useCase.updateSolicitud(null, 1L))
                .expectError(SolicitudValidationException.class)
                .verify();
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void updateSolicitud_errorNoEncontrada() {
        var solicitud = new Solicitud();
        when(solicitudRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateSolicitud(solicitud, 1L))
                .expectError(SolicitudNotFoundException.class)
                .verify();

        verify(solicitudRepository).findById(1L);
    }

    @Test
    void updateSolicitud_errorAlGuardar() {
        var solicitud = new Solicitud();
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(new Solicitud()));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateSolicitud(solicitud, 1L))
                .expectError(SolicitudUpdateException.class)
                .verify();

        verify(solicitudRepository).findById(1L);
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    void getAllSolicitud_ok() {
        var s1 = new Solicitud();
        when(solicitudRepository.findAll()).thenReturn(Flux.just(s1));

        StepVerifier.create(useCase.getAllSolicitud())
                .expectNext(s1)
                .verifyComplete();

        verify(solicitudRepository).findAll();
    }

    @Test
    void getAllSolicitud_sinRegistros() {
        when(solicitudRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.getAllSolicitud())
                .expectError(SolicitudException.class)
                .verify();

        verify(solicitudRepository).findAll();
    }

    @Test
    void getSolicitudesRevision_estadosPorDefecto() {
        var s1 = new Solicitud();
        when(solicitudRepository.findByIdestadoIn(List.of(2L,3L,4L))).thenReturn(Flux.just(s1));

        StepVerifier.create(useCase.getSolicitudesRevision(0, 5, null, null))
                .expectNext(s1)
                .verifyComplete();

        verify(solicitudRepository).findByIdestadoIn(List.of(2L,3L,4L));
    }

    @Test
    void getSolicitudesRevision_sinResultados() {
        when(solicitudRepository.findByIdestadoIn(List.of(2L))).thenReturn(Flux.empty());

        StepVerifier.create(useCase.getSolicitudesRevision(0, 10, "filtro", List.of(2L)))
                .expectError(SolicitudException.class)
                .verify();

        verify(solicitudRepository).findByIdestadoIn(List.of(2L));
    }

    @Test
    void getSolicitudById_ok() {
        var s = new Solicitud();
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(s));

        StepVerifier.create(useCase.getSolicitudById(1L))
                .expectNext(s)
                .verifyComplete();

        verify(solicitudRepository).findById(1L);
    }

    @Test
    void getSolicitudById_idNulo() {
        StepVerifier.create(useCase.getSolicitudById(null))
                .expectError(SolicitudValidationException.class)
                .verify();
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void getSolicitudById_noEncontrada() {
        when(solicitudRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getSolicitudById(1L))
                .expectError(SolicitudNotFoundException.class)
                .verify();

        verify(solicitudRepository).findById(1L);
    }

    @Test
    void deleteSolicitud_ok() {
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(new Solicitud()));
        when(solicitudRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteSolicitud(1L))
                .verifyComplete();

        verify(solicitudRepository).findById(1L);
        verify(solicitudRepository).deleteById(1L);
    }

    @Test
    void deleteSolicitud_idNulo() {
        StepVerifier.create(useCase.deleteSolicitud(null))
                .expectError(SolicitudValidationException.class)
                .verify();
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void deleteSolicitud_errorEliminar() {
        when(solicitudRepository.findById(1L)).thenReturn(Mono.just(new Solicitud()));
        when(solicitudRepository.deleteById(1L)).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(useCase.deleteSolicitud(1L))
                .expectError(SolicitudDeleteException.class)
                .verify();

        verify(solicitudRepository).findById(1L);
        verify(solicitudRepository).deleteById(1L);
    }

    @Test
    void findByEmail_ok() {
        var s = new Solicitud();
        when(solicitudRepository.findByEmail("user@test.com")).thenReturn(Mono.just(s));

        StepVerifier.create(useCase.findByEmail("user@test.com"))
                .expectNext(s)
                .verifyComplete();

        verify(solicitudRepository).findByEmail("user@test.com");
    }

    @Test
    void findByEmail_noExiste() {
        when(solicitudRepository.findByEmail("missing@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.findByEmail("missing@test.com"))
                .expectError(SolicitudException.class)
                .verify();

        verify(solicitudRepository).findByEmail("missing@test.com");
    }

    @Test
    void findByIdestadoIn_ok() {
        var s = new Solicitud();
        when(solicitudRepository.findByIdestadoIn(List.of(1L,2L))).thenReturn(Flux.just(s));

        StepVerifier.create(useCase.findByIdestadoIn(List.of(1L,2L)))
                .expectNext(s)
                .verifyComplete();

        verify(solicitudRepository).findByIdestadoIn(List.of(1L,2L));
    }
}
