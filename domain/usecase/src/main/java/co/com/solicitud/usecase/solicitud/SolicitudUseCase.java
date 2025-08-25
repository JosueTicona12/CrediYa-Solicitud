package co.com.solicitud.usecase.solicitud;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.gateways.SolicitudRepository;
import exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Log
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;

    public Mono<Solicitud> saveUser(Solicitud solicitud) {
        log.info("UseCase - Guardando solicitud");

        if (solicitud == null) {
            return Mono.error(new SolicitudValidationException("El servicio no puede ser nulo"));
        }
        if (solicitud.getMonto() == null || solicitud.getMonto().describeConstable().isEmpty()) {
            return Mono.error(new SolicitudValidationException("El monto es obligatorio"));
        }
        if (solicitud.getPlazo() == null) {
            return Mono.error(new SolicitudValidationException("El plazo es obligatorio"));
        }
        if (solicitud.getEmail() == null || solicitud.getEmail().isBlank()) {
            return Mono.error(new SolicitudValidationException("El correo electronico es obligatorio"));
        }
        return solicitudRepository.findByEmail(solicitud.getEmail())
                .flatMap(existing -> Mono.<Solicitud>error(
                        new SolicitudException("Ya existe un solicitud registrado con el email: " + solicitud.getEmail())
                ))
                .switchIfEmpty(solicitudRepository.save(solicitud)) // si no existe, lo guarda
                .doOnSuccess(u -> log.info("Solicitud guardado con éxito"))
                .doOnError(e -> log.severe("Error guardando solicitud: {}" + e.getMessage()));
    }

    public Mono<Solicitud> updateUser(Solicitud solicitud, Long id) {

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

    public Flux<Solicitud> getAllUsers() { log.info("UseCase - Buscar todos los usuarios");

        return solicitudRepository.findAll()
                .switchIfEmpty(Flux.error(new SolicitudException("No se encontraron solicitudes")))
                .doOnComplete(() -> log.info("Consulta de solicitudes completada"))
                .doOnError(e -> log.severe("Error consultando todas las solicitudes: " + e)); }

    public Mono<Solicitud> getUserById(Long id) {
        log.info("UseCase - Buscar solicitud por id {" + id + "}");

        if (id == null) {
            return Mono.error(new SolicitudValidationException("El id no puede ser nulo"));
        }

        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)))
                .doOnSuccess(u -> log.info("Solicitud encontrada: {" + u + "}"))
                .doOnError(e -> log.severe("Error buscando solicitud con id {" + id + "}: " + e)); }

    public Mono<Void> deleteUser(Long id) {
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
