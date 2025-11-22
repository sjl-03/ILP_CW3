package com.ilp.cw3.drone_simulator;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@SpringBootApplication
@EnableScheduling
public class DroneSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DroneSimulatorApplication.class, args);
    }

}
