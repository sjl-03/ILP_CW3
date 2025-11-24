package com.ilp.cw3.drone_simulator.simulation;

import com.ilp.cw3.drone_simulator.model.Drone;
import com.ilp.cw3.drone_simulator.model.SimulatedDroneInfo;
import com.ilp.cw3.drone_simulator.rabbitmq.TelemetrySender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulationManager {
    private static final Logger logger =
            LoggerFactory.getLogger(SimulationManager.class);

    private final TelemetrySender sender;
    // map droneId -> its simulator instance
    private final Map<String, DroneSimulatorInstance> simulatorMap =
            new ConcurrentHashMap<>();

    public static final int SECONDS_PER_TICK = 1;

    public SimulationManager(TelemetrySender sender) {
        this.sender = sender;
    }

    public void enqueueSimulation(SimulatedDroneInfo droneInfo){
        String droneId = droneInfo.droneDetail().id();

        simulatorMap.compute(droneId,
                (k,v) -> {
            if (v == null) {
                DroneSimulatorInstance simulatorInstance =
                        new DroneSimulatorInstance(sender, SECONDS_PER_TICK);
                simulatorInstance.enqueueTask(droneInfo);
                return simulatorInstance;
            }
            else {
                v.enqueueTask(droneInfo);
                return v;
            }
        });
    }
    @Scheduled(fixedRate = SECONDS_PER_TICK*1000)
    public void tick() {
        // call tick function for each drone
        simulatorMap.values().forEach(DroneSimulatorInstance::tick);

        simulatorMap.values().removeIf(simulator ->
                !simulator.isActive() && !simulator.hasPendingTasks());
    }
}
