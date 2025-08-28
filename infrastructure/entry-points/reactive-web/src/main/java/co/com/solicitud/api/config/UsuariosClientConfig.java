package co.com.solicitud.api.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class UsuariosClientConfig {

    @Bean
    public WebClient usuariosClient(@Value("${usuarios.base-url}") String baseUrl) {
        HttpClient http = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);

        return WebClient.builder()
                .baseUrl("http://localhost:8081/api/v1/")
                .clientConnector(new ReactorClientHttpConnector(http))
                .build();
    }
}
