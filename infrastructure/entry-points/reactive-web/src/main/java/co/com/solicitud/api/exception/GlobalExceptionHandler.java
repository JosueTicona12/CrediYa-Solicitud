package co.com.solicitud.api.exception;

import co.com.solicitud.api.config.ErrorResponse;
import co.com.solicitud.usecase.solicitud.utils.SolicitudStatusEnum;
import exceptions.SolicitudDeleteException;
import exceptions.SolicitudNotFoundException;
import exceptions.SolicitudUpdateException;
import exceptions.SolicitudValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class GlobalExceptionHandler {
    private Mono<ResponseEntity<ErrorResponse>> buildErrorResponse(
            Exception ex,
            HttpStatus status,
            SolicitudStatusEnum business,
            ServerHttpRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(business.code())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getURI().getPath())
                .build();

        return Mono.just(ResponseEntity.status(status).body(error));
    }

    @ExceptionHandler(SolicitudNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
            SolicitudNotFoundException ex,
            ServerHttpRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, SolicitudStatusEnum.SOLICITUD_NO_ENCONTRADA, request);
    }

    @ExceptionHandler(SolicitudValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            SolicitudValidationException ex,
            ServerHttpRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, SolicitudStatusEnum.VALIDACION_ERROR, request);
    }

    @ExceptionHandler(SolicitudUpdateException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUpdate(
            SolicitudUpdateException ex,
            ServerHttpRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, SolicitudStatusEnum.SOLICITUD_NO_ACTUALIZADA, request);
    }

    @ExceptionHandler(SolicitudDeleteException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDelete(
            SolicitudDeleteException ex,
            ServerHttpRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, SolicitudStatusEnum.SOLICITUD_NO_ELIMINADA, request);
    }

    @ExceptionHandler(Exception.class) // fallback para errores no controlados
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(
            Exception ex,
            ServerHttpRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, SolicitudStatusEnum.ERROR, request);
    }
}
