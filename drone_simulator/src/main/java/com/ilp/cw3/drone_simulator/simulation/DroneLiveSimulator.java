package com.ilp.cw3.drone_simulator.simulation;

import com.ilp.cw3.drone_simulator.model.*;
import com.ilp.cw3.drone_simulator.rabbitmq.TelemetryEvent;
import com.ilp.cw3.drone_simulator.rabbitmq.TelemetrySender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DroneLiveSimulator {
    private final TelemetrySender sender;
    private final SimulatedDronePath simulatedDronePath;
    private final String droneId;
    private final LocalDate leaveDate;
    private final LocalTime leaveTime;
    private final int servicePointId;
    private final int remainingMovesInitial;
    private final int SECONDS_PER_TICK = 6;
    private boolean finished = false;
    private final AtomicInteger globalIndex = new AtomicInteger(0);

    public DroneLiveSimulator(TelemetrySender sender, PathLoader pathLoader) {
        this.sender = sender;

        this.simulatedDronePath = pathLoader.loadPathJson();

        DroneDetail detail = pathLoader.loadDroneDetailJson();
        this.droneId = detail.id();
        this.leaveDate = detail.leaveDate();
        this.leaveTime = detail.leaveTime();
        this.servicePointId = detail.servicePointId();
        this.remainingMovesInitial = detail.remainingMoves();

        System.out.println("Simulator ready for drone " + droneId);
    }

    @Scheduled(fixedRate = SECONDS_PER_TICK*1000)
    public void tick(){

        if (finished) {
            return;
        }

        int globalTick = globalIndex.getAndIncrement();
        int remainingMoves = remainingMovesInitial - globalTick;
        LocalTime time = leaveTime.plusSeconds(
                (long) SECONDS_PER_TICK*globalTick);
        LocalDate date = (time.isBefore(leaveTime))
                ? leaveDate.plusDays(1) : leaveDate;

        Position nextPosition = simulatedDronePath.nextPosition();
        if (nextPosition == null) {
            // finish simulation
            finished = true;
            System.err.println("Simulator finished for drone " + droneId);
            return;
        }

        int deliveryId = simulatedDronePath.getCurrentDeliveryId();

        TelemetryEvent telemetry = new TelemetryEvent(
            droneId,
            nextPosition,
            remainingMoves,
            DroneStatus.FLYING,
            servicePointId,
            deliveryId,
            date,
            time
        );

        sender.send(telemetry);

    }
}
