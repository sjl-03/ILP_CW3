package com.ilp.cw3.drone_simulator.controller;

import com.ilp.cw3.drone_simulator.model.DeliveryPath;
import com.ilp.cw3.drone_simulator.model.MedDispatchRec;
import com.ilp.cw3.drone_simulator.rabbitmq.TelemetrySender;
import com.ilp.cw3.drone_simulator.service.SimulationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class SimulatorController {
    private static final Logger logger =
            LoggerFactory.getLogger(SimulatorController.class);
    private final SimulationService simulationService;

    public SimulatorController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/simulateDeliveryPath")
    public ResponseEntity<String> simulateDeliveryPath(
            @Valid @RequestBody(required = false) List<MedDispatchRec> req
    ){
        logger.info("Received /calcDeliveryPath request");

        simulationService.simulateDeliveryPath(req);
        return ResponseEntity.ok("Simulation started");
    }
}
