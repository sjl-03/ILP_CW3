package com.ilp.cw3.drone_simulator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Configuration
@EnableScheduling
public class CW2ClientConfig {
    private static final Logger logger =
            LoggerFactory.getLogger(CW2ClientConfig.class);

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // Chapter 8.20
    @Bean
    public WebClient webClient(
            @Value("${cw2.endpoint:http://localhost:8080}") String cw2Endpoint,
            WebClient.Builder webClientBuilder) {
        // 👇 ADD THESE LINES HERE
        logger.info("BASE URL IS: " + cw2Endpoint);
        logger.info("WEBCLIENT BEFORE BUILD: " + webClientBuilder);

        WebClient client = webClientBuilder
                .baseUrl(cw2Endpoint)
                .build();

        // 👇 ALSO PRINT THIS
        logger.info("WEBCLIENT AFTER BUILD: " + client);

        logger.info("cw2.endpoint: {}", cw2Endpoint);
        return client;
    }
}
