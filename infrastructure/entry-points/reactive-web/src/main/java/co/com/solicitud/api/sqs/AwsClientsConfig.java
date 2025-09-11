package co.com.solicitud.api.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.time.Duration;

@Configuration
public class AwsClientsConfig {

    @Value("${aws.region:us-east-2}")
    private String region;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        // Si se proporcionan credenciales explícitas, usarlas
        if (!accessKeyId.isEmpty() && !secretAccessKey.isEmpty()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            );
        }

        // De lo contrario, usar la cadena de proveedores por defecto de AWS
        return DefaultCredentialsProvider.create();
    }

    @Bean(destroyMethod = "close")
    public SqsAsyncClient sqsAsyncClient(AwsCredentialsProvider credentialsProvider) {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .httpClient(NettyNioAsyncHttpClient.builder()
                        .maxConcurrency(50)
                        .connectionTimeout(Duration.ofSeconds(3))
                        .build())
                .build();
    }
}