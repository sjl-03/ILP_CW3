package com.ilp.cw3.drone_visualiser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ilp.cw3.drone_visualiser.model.DroneWebSocketMessage;
import com.ilp.cw3.drone_visualiser.model.TelemetryEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.ilp.cw3.drone_visualiser.config.RabbitMQConfig.QUEUE_NAME;

@Service
public class TelemetryMsgService {
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
                event.position()
        );

        messagingTemplate.convertAndSend("/topic/drones", messageOut);

        System.out.println("Message sent to topic " + messageOut.droneId());
    }
}
