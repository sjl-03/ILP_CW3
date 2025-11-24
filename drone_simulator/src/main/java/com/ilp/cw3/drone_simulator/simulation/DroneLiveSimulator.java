//package com.ilp.cw3.drone_simulator.simulation;
//
//import com.ilp.cw3.drone_simulator.model.*;
//import com.ilp.cw3.drone_simulator.rabbitmq.TelemetryEvent;
//import com.ilp.cw3.drone_simulator.rabbitmq.TelemetrySender;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Component
//public class DroneLiveSimulator {
//
//    private static final Logger logger =
//            LoggerFactory.getLogger(DroneLiveSimulator.class);
//    private final TelemetrySender sender;
//    private SimulatedDronePath simulatedDronePath;
//    private String droneId;
//    private LocalDate leaveDate;
//    private LocalTime leaveTime;
//    private int servicePointId;
//    private int remainingMovesInitial;
//    private final int SECONDS_PER_TICK = 1;
//    private boolean active = false;
//    private final AtomicInteger globalIndex = new AtomicInteger(0);
//
//    public DroneLiveSimulator(TelemetrySender sender) {
//        this.sender = sender;
//        logger.info("Creating DroneLiveSimulator");
//    }
//
//    public void startSimulation(SimulatedDroneInfo droneInfo) {
//        this.active = false;
//
//        this.simulatedDronePath = droneInfo.simulatedDronePath();
//
//        DroneDetail detail = droneInfo.droneDetail();
//        this.droneId = detail.id();
//        this.leaveDate = detail.leaveDate();
//        this.leaveTime = detail.leaveTime();
//        this.servicePointId = detail.servicePointId();
//        this.remainingMovesInitial = detail.remainingMoves();
//
//        this.globalIndex.set(0);
//        this.active = true;
//        logger.info("Starting simulation for drone {}", droneId);
//    }
//
//    @Scheduled(fixedRate = SECONDS_PER_TICK*1000)
//    public void tick(){
//
//        if (!active) {
//            return;
//        }
//
//        int globalTick = globalIndex.getAndIncrement();
//        int remainingMoves = remainingMovesInitial - globalTick;
//        LocalTime time = leaveTime.plusSeconds(
//                (long) SECONDS_PER_TICK*globalTick);
//        LocalDate date = (time.isBefore(leaveTime))
//                ? leaveDate.plusDays(1) : leaveDate;
//
//        Position nextPosition = simulatedDronePath.nextPosition();
//        if (nextPosition == null) {
//            // finish simulation
//            active = false;
//            System.err.println("Simulator finished for drone " + droneId);
//            return;
//        }
//
//        int deliveryId = simulatedDronePath.getCurrentDeliveryId();
//
//        TelemetryEvent telemetry = new TelemetryEvent(
//                droneId,
//                nextPosition,
//                remainingMoves,
//                DroneStatus.FLYING,
//                servicePointId,
//                deliveryId,
//                date,
//                time
//        );
//
//        sender.send(telemetry);
//
//    }
//}
