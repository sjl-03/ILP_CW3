package com.ilp.cw3.drone_simulator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Configuration
@EnableScheduling
public class CW2ClientConfig {
    // Chapter 8.20
    @Bean
    public WebClient webClient(
            @Value("${cw2.endpoint:http://localhost:8080}") String cw2Endpoint,
            WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .baseUrl(cw2Endpoint)
                .build();
    }
}
