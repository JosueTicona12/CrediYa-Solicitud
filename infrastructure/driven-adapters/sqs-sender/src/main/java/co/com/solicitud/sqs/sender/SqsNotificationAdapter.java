package co.com.solicitud.sqs.sender;

import co.com.solicitud.model.solicitud.port.NotificacionPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;


import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsNotificationAdapter implements NotificacionPort {

    private final SqsAsyncClient sqs; // inyectado desde el Bean anterior
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${adapters.sqs.queueUrl}")
    private String queueUrl;

    @Override
    public Mono<Void> enviarNotificacion(String email, String estado) {
        return enviarNotificacionDetallada(Map.of("email", email, "idestado", estado));
    }

    public Mono<Void> enviarNotificacionDetallada(Map<String, Object> payload) {
        return Mono.fromCallable(() -> mapper.writeValueAsString(payload))
                .flatMap(body -> Mono.fromFuture(
                        sqs.sendMessage(SendMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .messageBody(body)
                                .build())
                ))
                .doOnSuccess(resp -> log.trace("SQS enviado. messageId={}", resp.messageId()))
                .doOnError(e -> log.error("Error enviando a SQS", e))
                .then();
    }
}
