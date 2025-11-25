package uk.ac.ed.acp.cw2.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.Position;
import uk.ac.ed.acp.cw2.dto.DistanceToRequest;
import uk.ac.ed.acp.cw2.dto.IsCloseToRequest;
import uk.ac.ed.acp.cw2.dto.InRegionRequest;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;
import uk.ac.ed.acp.cw2.services.GeometryService;

import java.net.URL;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;

    private final GeometryService geo;
    public ServiceController(GeometryService geo) {
        this.geo = geo;
    }


    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl +
                "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }

    @GetMapping("/uid")
    public String uid() {
        return "s2559435";
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(
            @Valid @RequestBody DistanceToRequest req) {
        logger.info("Received /distanceTo request: position1={}, position2={}",
                req.position1(), req.position2());
        return ResponseEntity.ok(geo.euclidianDistance(
                req.position1(), req.position2()));
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(
            @Valid @RequestBody IsCloseToRequest req) {
        logger.info("POST /isCloseTo");
        return ResponseEntity.ok(geo.isCloseTo(
                req.position1(), req.position2()));
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<Position> nextPosition(
            @Valid @RequestBody NextPositionRequest req) {
        logger.info("POST /nextPosition");
        return ResponseEntity.ok(geo.nextPosition(
                req.start(), req.angle()
        ));
    }

    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(
            @Valid @RequestBody InRegionRequest req) {
        logger.info("POST /isInRegion region={}", req.region().name());
        return ResponseEntity.ok(geo.isInRegion(
                req.position(), req.region()
        ));
    }

}
