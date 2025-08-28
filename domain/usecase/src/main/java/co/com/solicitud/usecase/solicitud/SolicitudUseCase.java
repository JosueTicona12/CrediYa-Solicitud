package co.com.solicitud.usecase.solicitud;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.dto.SolicitudCreacionDTO;
import co.com.solicitud.model.solicitud.gateways.SolicitudRepository;
import co.com.solicitud.model.solicitud.port.UsuarioPort;
import exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RequiredArgsConstructor
@Log
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioPort usuarioPort;

    public Mono<Solicitud> crearSolicitud(SolicitudCreacionDTO creacion) {

        if (creacion.documento() == null || creacion.documento().isBlank()) {
            return Mono.error(new SolicitudValidationException("El documento es obligatorio"));
        }
        if (creacion.monto() == null || creacion.monto() <= 0) {
            return Mono.error(new SolicitudValidationException("El monto debe ser > 0"));
        }
        if (creacion.plazo() == null || !creacion.plazo().isAfter(java.time.LocalDate.now())) {
            return Mono.error(new SolicitudValidationException("El plazo debe ser una fecha futura"));
        }

        return usuarioPort.getByDocumento(creacion.documento())
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(creacion.documento())))
                .flatMap(usuarioAuth -> {
                    if (usuarioAuth.activo() == null || usuarioAuth.activo() == 0L) {
                        return Mono.error(new SolicitudException("El usuario está inactivo"));
                    }

                    final String email = usuarioAuth.email();
                    if (email == null || email.isBlank()) {
                        return Mono.error(new SolicitudValidationException("El usuario no tiene email válido"));
                    }
                    return solicitudRepository.findByEmail(email)
                            .flatMap(ex -> Mono.<Solicitud>error(
                                    new SolicitudException("Ya existe una solicitud registrada con el email: " + email)
                            ))
                            .switchIfEmpty(Mono.defer(() -> {
                                Solicitud solicitud = new Solicitud();
                                solicitud.setMonto(creacion.monto());
                                solicitud.setPlazo(creacion.plazo());
                                solicitud.setEmail(email);
                                solicitud.setIdestado(creacion.idestado());
                                solicitud.setIdtipoprestamo(1L); // estado inicial (ejemplo)
                                return solicitudRepository.save(solicitud);
                            }));
                });
    }

    public Mono<Solicitud> updateSolicitud(Solicitud solicitud, Long id) {

        log.info("UseCase - Actualizando solicitud con id {}");

        if (id == null) {
            return Mono.error(new SolicitudValidationException("El id no puede ser nulo"));
        }
        if (solicitud == null) {
            return Mono.error(new SolicitudValidationException("La solciitud no puede ser nulo"));
        }

        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)))
                .flatMap(existing -> {
                    existing.setMonto(solicitud.getMonto());
                    existing.setEmail(solicitud.getEmail());
                    existing.setPlazo(solicitud.getPlazo());
                    existing.setIdestado(solicitud.getIdestado());
                    existing.setIdtipoprestamo(solicitud.getIdtipoprestamo());
                    return solicitudRepository.save(existing);
                })
                .switchIfEmpty(Mono.error(new SolicitudUpdateException(id)))
                .doOnSuccess(u -> log.info("Solicitud actualizado: {" + id + "}"))
                .doOnError(e -> log.severe("Error actualizando solicitud con id {" + id + "}"));
    }

    public Flux<Solicitud> getAllSolicitud() { log.info("UseCase - Buscar todos los usuarios");

        return solicitudRepository.findAll()
                .switchIfEmpty(Flux.error(new SolicitudException("No se encontraron solicitudes")))
                .doOnComplete(() -> log.info("Consulta de solicitudes completada"))
                .doOnError(e -> log.severe("Error consultando todas las solicitudes: " + e)); }

    public Mono<Solicitud> getSolicitudById(Long id) {
        log.info("UseCase - Buscar solicitud por id {" + id + "}");

        if (id == null) {
            return Mono.error(new SolicitudValidationException("El id no puede ser nulo"));
        }

        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)))
                .doOnSuccess(u -> log.info("Solicitud encontrada: {" + u + "}"))
                .doOnError(e -> log.severe("Error buscando solicitud con id {" + id + "}: " + e)); }

    public Mono<Void> deleteSolicitud(Long id) {
        log.info("UseCase - Eliminando solicitud con id {" + id + "}");

        if (id == null) {
            return Mono.error(new SolicitudValidationException("El id no puede ser nulo"));
        }

        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)))
                .flatMap(existing -> solicitudRepository.deleteById(id))
                .doOnSuccess(v -> log.info("Solicitud eliminado con id {" + id + "}"))
                .doOnError(e -> {
                    log.severe("Error eliminando solicitud con id {" + id +"}: " + e);
                    throw new SolicitudDeleteException(id);
                });
    }

    public Mono<Solicitud> findByEmail(String email) {
        log.info("UseCase - Busqueda de Id por correo");
        return solicitudRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new SolicitudException("El campo email esta vacio" + email)))
                .doOnSuccess(u -> log.info("Usuario encontrado: {" + u + "}"))
                .doOnError(e -> log.severe("Error buscando usuario con email {" + email + "}: " + e));
    }
}
