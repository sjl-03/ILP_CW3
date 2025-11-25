package com.ilp.cw3.drone_visualiser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ilp.cw3.drone_visualiser.model.DroneWebSocketMessage;
import com.ilp.cw3.drone_visualiser.model.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.ilp.cw3.drone_visualiser.config.RabbitMQConfig.QUEUE_NAME;

@Service
public class TelemetryMsgService {
    private static final Logger logger =
            LoggerFactory.getLogger(TelemetryMsgService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public TelemetryMsgService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    @RabbitListener(queues = QUEUE_NAME)
    public void listen(String messageIn) throws Exception {
//        System.out.println("Received message: " + message);
        TelemetryEvent event = objectMapper.readValue(messageIn, TelemetryEvent.class);

        DroneWebSocketMessage messageOut = new DroneWebSocketMessage(
                event.droneId(),
                event.status(),
                event.position(),
                event.dispatchId(),
                event.remainingMoves()
        );

        messagingTemplate.convertAndSend("/topic/drones", messageOut);

        logger.debug("Message sent to topic: droneId={}, eventId={}, position={}",
                messageOut.droneId(), event.status(), event.position());
    }
}
