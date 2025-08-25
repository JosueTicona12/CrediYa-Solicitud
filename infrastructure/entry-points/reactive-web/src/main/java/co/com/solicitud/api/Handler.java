package co.com.solicitud.api;

import co.com.solicitud.api.config.ErrorResponse;
import co.com.solicitud.api.config.SuccessResponse;
import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.usecase.solicitud.SolicitudUseCase;
import exceptions.SolicitudDeleteException;
import exceptions.SolicitudNotFoundException;
import exceptions.SolicitudUpdateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
//private  final UseCase useCase;
//private  final UseCase2 useCase2;

    private final SolicitudUseCase solicitudUseCase;

    public Mono<ServerResponse> listenSaveSolicitud(ServerRequest request) {
        log.trace("Handler - Recibida petición de guardado para solicitud");

        return request.bodyToMono(Solicitud.class)
                .flatMap(solicitudUseCase::saveUser)
                .flatMap(u -> {
                    SuccessResponse response = SuccessResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(201)
                            .message("Solicitud creada correctamente")
                            .build();
                    return ServerResponse.status(201)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }

    public Mono<ServerResponse> listenUpdateSolicitud(ServerRequest request) {
        String id = request.pathVariable("id");
        log.trace("Handler - Recibida petición de actualización para solicitud con id={}", id);

        return request.bodyToMono(Solicitud.class)
                .flatMap(usuario -> solicitudUseCase.updateUser(usuario, Long.valueOf(id)))
                .flatMap(u -> {
                    SuccessResponse response = SuccessResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.OK.value())
                            .message("Solicitud actualizado correctamente")
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(SolicitudNotFoundException.class,
                        e -> buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request))
                .onErrorResume(SolicitudUpdateException.class,
                        e -> buildErrorResponse(HttpStatus.CONFLICT, e.getMessage(), request));
    }


    public Mono<ServerResponse> listenGetAllSolicitud(ServerRequest request) {
        log.trace("Handler - Recibida petición de obtener todas las solicitudes");

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(solicitudUseCase.getAllUsers(), Solicitud.class)
                .onErrorResume(Exception.class,
                        e -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request));
    }

    public Mono<ServerResponse> listenSolicitudById(ServerRequest request) {
        String id = request.pathVariable("id");
        log.trace("Handler - Recibida petición de obtener solicitud con id={}", id);

        return solicitudUseCase.getUserById(Long.valueOf(id))
                .flatMap(usuario -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(usuario))
                .onErrorResume(SolicitudNotFoundException.class,
                        e -> buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request));
    }

    public Mono<ServerResponse> listenDeleteSolicitud(ServerRequest request) {
        String id = request.pathVariable("id");
        log.trace("Handler - Recibida petición de eliminar solicitud con id={}", id);

        return solicitudUseCase.deleteUser(Long.valueOf(id))
                .then(ServerResponse.noContent().build())
                .onErrorResume(SolicitudNotFoundException.class,
                        e -> buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request))
                .onErrorResume(SolicitudDeleteException.class,
                        e -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request));
    }

    private Mono<ServerResponse> buildErrorResponse(HttpStatus status, String message, ServerRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.path())
                .build();

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error);
    }
}
