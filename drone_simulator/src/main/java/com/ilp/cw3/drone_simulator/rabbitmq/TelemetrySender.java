package com.ilp.cw3.drone_simulator.rabbitmq;

import com.ilp.cw3.drone_simulator.config.RabbitMQConfig;
import com.ilp.cw3.drone_simulator.service.SimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TelemetrySender {
    private static final Logger logger =
            LoggerFactory.getLogger(TelemetrySender.class);
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

        logger.debug("Sent telemetry event: {}", telemetryEvent);
    }
}
