package com.ilp.cw3.drone_simulator.service;

import com.ilp.cw3.drone_simulator.model.DeliveryPath;
import com.ilp.cw3.drone_simulator.model.DronePath;
import com.ilp.cw3.drone_simulator.model.SimulatedDelivery;
import com.ilp.cw3.drone_simulator.model.SimulatedDronePath;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PathConversionService {
    public SimulatedDronePath convertPathToSimulatedDronePath(
            DronePath dronePath
    ){
        List<SimulatedDelivery> simulatedDeliveries =
                dronePath.deliveries().stream()
                        .map(SimulatedDelivery::new).toList();
        return new SimulatedDronePath(
                dronePath.droneId(), simulatedDeliveries);
    }
}
