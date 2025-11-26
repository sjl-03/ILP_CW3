package com.ilp.cw3.drone_simulator.simulation;

import com.ilp.cw3.drone_simulator.model.*;
import com.ilp.cw3.drone_simulator.rabbitmq.TelemetryEvent;
import com.ilp.cw3.drone_simulator.rabbitmq.TelemetrySender;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DroneSimulatorInstance {

    private static final Logger logger =
            LoggerFactory.getLogger(DroneSimulatorInstance.class);
    private final TelemetrySender sender;

    // queue per drone
    private final Queue<SimulatedDroneInfo> taskQueue =
            new ConcurrentLinkedQueue<>();

    private SimulatedDronePath simulatedDronePath;
    private String droneId;
    private LocalDate leaveDate;
    private LocalTime leaveTime;
    private int servicePointId;
    private int remainingMovesInitial;
    @Getter
    private boolean active = false;
    private final AtomicInteger globalIndex = new AtomicInteger(0);
    private final int SECONDS_PER_TICK;


    public DroneSimulatorInstance(TelemetrySender sender, int secondsPerTick) {
        this.sender = sender;
        this.SECONDS_PER_TICK = secondsPerTick;
        logger.info("Creating DroneSimulatorInstance");
    }

    public boolean hasPendingTasks() {
        return !taskQueue.isEmpty();
    }
    public void enqueueTask(SimulatedDroneInfo newInfo) {
        taskQueue.add(newInfo);
    }

    public void startSimulation(SimulatedDroneInfo droneInfo) {
        this.active = false;

        this.simulatedDronePath = droneInfo.simulatedDronePath();

        DroneDetail detail = droneInfo.droneDetail();
        this.droneId = detail.id();
        this.leaveDate = detail.leaveDate();
        this.leaveTime = detail.leaveTime();
        this.servicePointId = detail.servicePointId();
        this.remainingMovesInitial = detail.remainingMoves();

        this.globalIndex.set(0);
        this.active = true;
        logger.info("Starting simulation for drone {}", droneId);
    }

    public void tick(){

        if (!active) {
            SimulatedDroneInfo next = taskQueue.poll();
            if (next == null) {
                return;
            }
            startSimulation(next);
        }

        int globalTick = globalIndex.getAndIncrement();
        int remainingMoves = remainingMovesInitial - globalTick;
        LocalTime time = leaveTime.plusSeconds(
                (long) SECONDS_PER_TICK*globalTick);
        LocalDate date = (time.isBefore(leaveTime))
                ? leaveDate.plusDays(1) : leaveDate;

        Position nextPosition = simulatedDronePath.nextPosition();
        int deliveryId = simulatedDronePath.getCurrentDeliveryId();

        if (nextPosition == null) {
            // finish simulation
            active = false;
            logger.info("Simulator finished for drone {}",droneId);
            TelemetryEvent telemetry = new TelemetryEvent(
                    droneId,
                    null,
                    remainingMoves,
                    DroneStatus.IDLE,
                    servicePointId,
                    deliveryId,
                    date,
                    time
            );

            sender.send(telemetry);
            return;
        }

        if (remainingMoves <= 0) {
            logger.warn("Running out of moves with droneId {}", droneId);
            active = false;
            TelemetryEvent telemetry = new TelemetryEvent(
                    droneId,
                    nextPosition,
                    remainingMoves,
                    DroneStatus.ERROR,
                    servicePointId,
                    deliveryId,
                    date,
                    time
            );
            sender.send(telemetry);
            return;
        }

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
