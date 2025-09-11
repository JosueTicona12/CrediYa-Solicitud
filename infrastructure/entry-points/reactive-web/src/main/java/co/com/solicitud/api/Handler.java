package co.com.solicitud.api;

import co.com.solicitud.api.config.ErrorResponse;
import co.com.solicitud.api.config.SuccessResponse;
import co.com.solicitud.api.security.JwtUtil;
import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.model.solicitud.SolicitudCreacion;
import co.com.solicitud.usecase.solicitud.SolicitudUseCase;
import co.com.solicitud.usecase.solicitud.utils.SolicitudErrorEnum;
import co.com.solicitud.usecase.solicitud.utils.SolicitudLogEnum;
import co.com.solicitud.usecase.solicitud.utils.SolicitudStatusEnum;
import exceptions.SolicitudDeleteException;
import exceptions.SolicitudNotFoundException;
import exceptions.SolicitudUpdateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final SolicitudUseCase solicitudUseCase;

    public Mono<ServerResponse> listenSaveSolicitud(ServerRequest req) {
        String authHeader = req.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        return req.bodyToMono(SolicitudCreacion.class)
                .flatMap(dto -> {
                    if (!JwtUtil.isClient(authHeader)) {
                        return buildErrorResponse(HttpStatus.UNAUTHORIZED,
                                SolicitudStatusEnum.UNAUTHORIZED,
                                SolicitudErrorEnum.TOKEN_INVALIDO_CLIENTE.message(),
                                req);
                    }
                    String token = JwtUtil.extractToken(authHeader);
                    String email = JwtUtil.getEmail(token);
                    return solicitudUseCase.crearSolicitud(dto, email)
                            .contextWrite(ctx -> ctx.put("authToken", token))
                            .flatMap(saved -> {
                                SuccessResponse response = SuccessResponse.builder()
                                        .timestamp(LocalDateTime.now())
                                        .status(SolicitudStatusEnum.SOLICITUD_CREADA.code())
                                        .message("Solicitud creada correctamente")
                                        .build();
                                return ServerResponse.status(HttpStatus.CREATED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(response);
                            });
                })
                .onErrorResume(e -> buildErrorResponse(HttpStatus.BAD_REQUEST,
                        SolicitudStatusEnum.VALIDACION_ERROR,
                        e.getMessage(),
                        req));
    }

    public Mono<ServerResponse> listenUpdateSolicitud(ServerRequest request) {
        String id = request.pathVariable("id");
        log.trace(SolicitudLogEnum.PETICION_ACTUALIZACION.message() + id);
        String authHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        if (!JwtUtil.isAsesor(authHeader)) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED,
                    SolicitudStatusEnum.UNAUTHORIZED,
                    SolicitudErrorEnum.TOKEN_INVALIDO_ASESOR.message(),
                    request);
        }
        return request.bodyToMono(Solicitud.class)
                .flatMap(usuario -> solicitudUseCase.updateSolicitud(usuario, Long.valueOf(id)))
                .flatMap(u -> {
                    SuccessResponse response = SuccessResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(SolicitudStatusEnum.SOLICITUD_ACTUALIZADA.code())
                            .message("Solicitud actualizado correctamente")
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(SolicitudNotFoundException.class,
                        e -> buildErrorResponse(HttpStatus.NOT_FOUND,
                                SolicitudStatusEnum.SOLICITUD_NO_ENCONTRADA,
                                e.getMessage(),
                                request))
                .onErrorResume(SolicitudUpdateException.class,
                        e -> buildErrorResponse(HttpStatus.CONFLICT,
                                SolicitudStatusEnum.SOLICITUD_NO_ACTUALIZADA,
                                e.getMessage(),
                                request));
    }


    public Mono<ServerResponse> listenGetAllSolicitud(ServerRequest request) {
        log.trace(SolicitudLogEnum.PETICION_OBTENER_TODAS.message());

        return solicitudUseCase.getAllSolicitud().collectList()
                .flatMap(list -> {
                    SuccessResponse response = SuccessResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(SolicitudStatusEnum.SOLICITUDES_LISTADAS.code())
                            .message("Solicitudes listadas correctamente")
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(Exception.class,
                        e -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                SolicitudStatusEnum.ERROR,
                                e.getMessage(),
                                request));
    }

    public Mono<ServerResponse> listenGetSolicitudesRevision(ServerRequest request) {
        log.trace("Handler - Recibida petición de obtener solicitudes para revisión");
        String authHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        if (!JwtUtil.isAsesor(authHeader)) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED,
                    SolicitudStatusEnum.UNAUTHORIZED,
                    SolicitudErrorEnum.TOKEN_INVALIDO_ASESOR.message(),
                    request);
        }
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String filtro = request.queryParam("filtro").orElse("");
        String estadosParam = request.queryParam("estados").orElse("");
        List<Long> estados = Arrays.stream(estadosParam.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .toList();

        return solicitudUseCase.getSolicitudesRevision(page, size, filtro, estados).collectList()
                .flatMap(list -> {
                    SuccessResponse response = SuccessResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(SolicitudStatusEnum.SOLICITUDES_LISTADAS.code())
                            .message("Solicitudes listadas correctamente")
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(Exception.class,
                        e -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                SolicitudStatusEnum.ERROR,
                                e.getMessage(),
                                request));
    }

    public Mono<ServerResponse> listenSolicitudById(ServerRequest request) {
        String id = request.pathVariable("id");
        log.trace(SolicitudLogEnum.PETICION_OBTENER_ID.message() + id);

        return solicitudUseCase.getSolicitudById(Long.valueOf(id))
                .flatMap(usuario -> {
                    SuccessResponse response = SuccessResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(SolicitudStatusEnum.SOLICITUD_ENCONTRADA.code())
                            .message("Solicitud encontrada")
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(SolicitudNotFoundException.class,
                        e -> buildErrorResponse(HttpStatus.NOT_FOUND,
                                SolicitudStatusEnum.SOLICITUD_NO_ENCONTRADA,
                                e.getMessage(),
                                request));
    }

    public Mono<ServerResponse> listenDeleteSolicitud(ServerRequest request) {
        String id = request.pathVariable("id");
        log.trace(SolicitudLogEnum.PETICION_ELIMINAR.message() + id);

        return solicitudUseCase.deleteSolicitud(Long.valueOf(id))
                .then(Mono.defer(() -> {
                    SuccessResponse response = SuccessResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(SolicitudStatusEnum.SOLICITUD_ELIMINADA.code())
                            .message("Solicitud eliminada correctamente")
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                }))
                .onErrorResume(SolicitudNotFoundException.class,
                        e -> buildErrorResponse(HttpStatus.NOT_FOUND,
                                SolicitudStatusEnum.SOLICITUD_NO_ENCONTRADA,
                                e.getMessage(),
                                request))
                .onErrorResume(SolicitudDeleteException.class,
                        e -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                SolicitudStatusEnum.SOLICITUD_NO_ELIMINADA,
                                e.getMessage(),
                                request));
    }

    private Mono<ServerResponse> buildErrorResponse(HttpStatus status, SolicitudStatusEnum business, String message, ServerRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(business.code())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.path())
                .build();
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error);
    }
}
