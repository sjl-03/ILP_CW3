package com.ilp.cw3.drone_simulator.rabbitmq;

import com.ilp.cw3.drone_simulator.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TelemetrySender {
    private final RabbitTemplate rabbitTemplate;
    public TelemetrySender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(TelemetryEvent telemetryEvent) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
                telemetryEvent
        );
        System.out.println("Telemetry sent to " + telemetryEvent);
    }
}
