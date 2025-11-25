package uk.ac.ed.acp.cw2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@EnableScheduling
public class IlpRestServiceConfig {
    // Chapter 8.20
    @Bean
    public WebClient webClient(
            @Value("${ilp.service.url}") String ilpEndpoint,
            WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .baseUrl(ilpEndpoint)
                .build();
    }
}
