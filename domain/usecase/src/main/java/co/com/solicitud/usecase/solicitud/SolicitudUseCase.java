package co.com.solicitud.usecase.solicitud;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.SolicitudCreacion;
import co.com.solicitud.model.solicitud.SolicitudEstadoUpdate;
import co.com.solicitud.model.solicitud.gateways.SolicitudRepository;
import co.com.solicitud.model.solicitud.port.NotificacionPort;
import co.com.solicitud.model.solicitud.port.UsuarioPort;
import co.com.solicitud.model.tipoprestamo.TipoPrestamo;
import co.com.solicitud.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.solicitud.usecase.solicitud.utils.SolicitudErrorEnum;
import co.com.solicitud.usecase.solicitud.utils.SolicitudLogEnum;
import exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collection;

@RequiredArgsConstructor
@Log
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioPort usuarioPort;
    private final NotificacionPort notificacionPort;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    private static final long ESTADO_APROBADO_AUTOMATICO = 1L;
    private static final long ESTADO_EN_REVISION = 2L;
    private static final long TIPO_PRESTAMO_POR_DEFECTO = 1L;

    public Mono<Solicitud> crearSolicitud(SolicitudCreacion creacion, String emailToken) {

        if (creacion.documento() == null || creacion.documento().isBlank()) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.DOCUMENTO_OBLIGATORIO.message()));
        }
        if (creacion.monto() == null || creacion.monto() <= 0) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.MONTO_INVALIDO.message()));
        }
        if (creacion.plazo() == null || !creacion.plazo().isAfter(LocalDate.now())) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.PLAZO_INVALIDO.message()));
        }

        return usuarioPort.getByDocumento(creacion.documento())
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(creacion.documento())))
                .flatMap(usuarioAuth -> {
                    if (usuarioAuth.activo() == null || usuarioAuth.activo() == 0L) {
                        return Mono.error(new SolicitudException(SolicitudErrorEnum.USUARIO_INACTIVO.message()));
                    }

                    final String email = usuarioAuth.email();
                    if (email == null || email.isBlank()) {
                        return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.USUARIO_EMAIL_INVALIDO.message()));
                    }
                    if (!email.equalsIgnoreCase(emailToken)) {
                        return Mono.error(new SolicitudException(SolicitudErrorEnum.TOKEN_NO_PERTENECE.message()));
                    }
                    return solicitudRepository.findByEmail(email)
                            .flatMap(ex -> Mono.<Solicitud>error(
                                    new SolicitudException(String.format(SolicitudErrorEnum.SOLICITUD_EXISTE.message(), email))
                            ))
                            .switchIfEmpty(Mono.defer(() -> {
                                Long tipoPrestamoId = creacion.idTipoPrestamo();
                                if (tipoPrestamoId == null || tipoPrestamoId == 0L) {
                                    tipoPrestamoId = TIPO_PRESTAMO_POR_DEFECTO;
                                }
                                final Long finalTipoPrestamoId = tipoPrestamoId;
                                return tipoPrestamoRepository.findById(finalTipoPrestamoId)
                                        .switchIfEmpty(Mono.error(new SolicitudValidationException(
                                                SolicitudErrorEnum.TIPO_PRESTAMO_NO_ENCONTRADO.message())))
                                        .flatMap(tipoPrestamo -> {
                                            Solicitud solicitud = new Solicitud();
                                            solicitud.setMonto(creacion.monto());
                                            solicitud.setPlazo(creacion.plazo());
                                            solicitud.setEmail(email);
                                            solicitud.setDocumento(creacion.documento());
                                            solicitud.setIdtipoprestamo(tipoPrestamo.getId());
                                            Long estadoInicial = getALong(creacion, tipoPrestamo);
                                            solicitud.setIdestado(estadoInicial);
                                            return solicitudRepository.save(solicitud);
                                        });
                            }));
                });
    }


    public Mono<Solicitud> updateSolicitud(Solicitud solicitud, Long id) {

        log.info(SolicitudLogEnum.ACTUALIZAR_SOLICITUD.message() + id);

        if (id == null) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.ID_NULO.message()));
        }
        if (solicitud == null) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.SOLICITUD_NULA.message()));
        }
        if (solicitud.getEmail() == null || solicitud.getEmail().isBlank()) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.EMAIL_OBLIGATORIO.message()));
        }
        if (solicitud.getIdestado() == null) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.IDESTADO_OBLIGATORIO.message()));
        }
        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)))
                .flatMap(existing -> {
                    existing.setEmail(solicitud.getEmail());
                    existing.setIdestado(solicitud.getIdestado());
                    if (solicitud.getMonto() != null) {
                        existing.setMonto(solicitud.getMonto());
                    }
                    if (solicitud.getPlazo() != null) {
                        existing.setPlazo(solicitud.getPlazo());
                    }
                    if (solicitud.getMonto() != null) {
                        existing.setMonto(solicitud.getMonto());
                    }
                    return solicitudRepository.save(existing)
                            .flatMap(saved -> {
                                if (solicitud.getIdestado() != null &&
                                        (solicitud.getIdestado().equals(1L) || solicitud.getIdestado().equals(3L))) {
                                    String estado = solicitud.getIdestado().equals(1L) ? "Aprobado" : "Rechazado";
                                    return notificacionPort.enviarNotificacion(saved.getEmail(), estado)
                                            .thenReturn(saved);
                                }
                                return Mono.just(saved);
                            });
                })
                .switchIfEmpty(Mono.error(new SolicitudUpdateException(id)))
                .doOnSuccess(u -> log.info(SolicitudLogEnum.SOLICITUD_ACTUALIZADA.message() + id))
                .doOnError(e -> log.severe(SolicitudLogEnum.ERROR_ACTUALIZAR_SOLICITUD.message() + id));
    }

    public Flux<Solicitud> getAllSolicitud() {
        log.info(SolicitudLogEnum.BUSCAR_TODAS_SOLICITUDES.message());

        return solicitudRepository.findAll()
                .switchIfEmpty(Flux.error(new SolicitudException(SolicitudErrorEnum.NO_SOLICITUDES.message())))
                .doOnComplete(() -> log.info(SolicitudLogEnum.CONSULTA_COMPLETADA.message()))
                .doOnError(e -> log.severe(SolicitudLogEnum.ERROR_CONSULTA_SOLICITUDES.message() + e));
    }

    public Flux<Solicitud> getSolicitudesRevision(int page, int size, String filtro, Collection<Long> estados) {
        log.info(SolicitudLogEnum.OBTENER_SOLICITUDES_REVISION.message());
        Collection<Long> estadosFiltrar = (estados == null || estados.isEmpty())
                ? java.util.List.of(2L, 3L, 4L)
                : estados;
        return solicitudRepository.findByIdestadoIn(estadosFiltrar)
                .filter(s -> filtro == null || filtro.isBlank() || s.getEmail().contains(filtro))
                .skip((long) page * size)
                .take(size)
                .switchIfEmpty(Flux.error(new SolicitudException(SolicitudErrorEnum.NO_SOLICITUDES_REVISION.message())));
    }

    public Mono<Solicitud> getSolicitudById(Long id) {
        log.info(SolicitudLogEnum.BUSCAR_SOLICITUD_POR_ID.message() + id);

        if (id == null) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.ID_NULO.message()));
        }

        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)))
                .doOnSuccess(u -> log.info(SolicitudLogEnum.SOLICITUD_ENCONTRADA.message() + u))
                .doOnError(e -> log.severe(SolicitudLogEnum.ERROR_SOLICITUD_POR_ID.message() + id + ": " + e));
    }

    public Mono<Void> deleteSolicitud(Long id) {
        log.info(SolicitudLogEnum.ELIMINAR_SOLICITUD.message() + id);

        if (id == null) {
            return Mono.error(new SolicitudValidationException(SolicitudErrorEnum.ID_NULO.message()));
        }

        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)))
                .flatMap(existing -> solicitudRepository.deleteById(id))
                .doOnSuccess(v -> log.info(SolicitudLogEnum.SOLICITUD_ELIMINADA.message() + id))
                .onErrorMap(e -> {
                    log.severe(SolicitudLogEnum.ERROR_ELIMINAR_SOLICITUD.message() + id + ": " + e);
                    return new SolicitudDeleteException(id);
                })
                .doOnSuccess(v -> log.info(SolicitudLogEnum.SOLICITUD_ELIMINADA.message() + id));
    }

    public Mono<Solicitud> findByEmail(String email) {
        log.info(SolicitudLogEnum.BUSCAR_POR_EMAIL.message());
        return solicitudRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new SolicitudException(String.format(SolicitudErrorEnum.EMAIL_VACIO.message(), email))))
                .doOnSuccess(u -> log.info(SolicitudLogEnum.USUARIO_ENCONTRADO.message() + u))
                .doOnError(e -> log.severe(SolicitudLogEnum.ERROR_BUSCAR_POR_EMAIL.message() + email + ": " + e));
    }

    public Flux<Solicitud> findByIdestadoIn(Collection<Long> estados) {
        return solicitudRepository.findByIdestadoIn(estados);
    }

    private static Long getALong(SolicitudCreacion creacion, TipoPrestamo tipoPrestamo) {
        Long estadoInicial;
        if (Boolean.TRUE.equals(tipoPrestamo.getSolAut())) {
            estadoInicial = ESTADO_APROBADO_AUTOMATICO;
        } else if (creacion.idestado() != null && !creacion.idestado().equals(ESTADO_APROBADO_AUTOMATICO)) {
            estadoInicial = creacion.idestado();
        } else {
            estadoInicial = ESTADO_EN_REVISION;
        }
        return estadoInicial;
    }
}
