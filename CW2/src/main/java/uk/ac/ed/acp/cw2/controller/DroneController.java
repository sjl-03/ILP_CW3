package uk.ac.ed.acp.cw2.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.DeliveryPath;
import uk.ac.ed.acp.cw2.data.Drone;
import uk.ac.ed.acp.cw2.data.MedDispatchRec;
import uk.ac.ed.acp.cw2.data.Query;
import uk.ac.ed.acp.cw2.data.geoJson.GeoJsonFeatureCollection;
import uk.ac.ed.acp.cw2.services.PathPlanningService;
import uk.ac.ed.acp.cw2.services.DroneQueriesService;

import java.net.URL;
import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class DroneController {
    private static final Logger logger =
            LoggerFactory.getLogger(DroneController.class);
    @Value("${ilp.service.url}")
    public URL serviceUrl;

    private final DroneQueriesService droneQueryService;
    private final PathPlanningService pathPlanningService;

    public DroneController(DroneQueriesService droneQueryService,
                           PathPlanningService pathPlanningService){
        this.droneQueryService = droneQueryService;
        this.pathPlanningService = pathPlanningService;
    }

    @GetMapping("/dronesWithCooling/{state}")
    public ResponseEntity<List<String>> dronesWithCooling
            (@PathVariable boolean state) {
        logger.info("Received /dronesWithCooling/{} request", state);
        return ResponseEntity.ok(
                droneQueryService.getDronesWithCooling(state));
    }

    @GetMapping("/droneDetails/{droneId}")
    public ResponseEntity<Drone> droneDetails
            (@PathVariable String droneId) {
        logger.info("Received /droneDetails/{} request", droneId);
        return ResponseEntity.ok(
                droneQueryService.getDroneDetails(droneId));
    }

    @GetMapping("/queryAsPath/{attributeName}/{attributeValue}")
    public ResponseEntity<List<String>> queryAsPath
            (@PathVariable String attributeName,
             @PathVariable String attributeValue) {
        logger.info("Received /queryAsPath/{}/{} request",
                attributeName,
                attributeValue);
        return ResponseEntity.ok(
                droneQueryService.queryAsPath(attributeName, attributeValue));
    }

    @PostMapping("/query")
    public ResponseEntity<List<String>> query(
            @Valid @RequestBody List<Query> req) {
        logger.info("Received /query request");
        return ResponseEntity.ok(droneQueryService.query(req));
    }

    @PostMapping("/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(
            @Valid @RequestBody List<MedDispatchRec> req
    ){
        logger.info("Received /queryAvailableDrones request");
        return ResponseEntity.ok(pathPlanningService.queryAvailableDrones(req));
    }

    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<DeliveryPath> calcDeliveryPath(
            @Valid @RequestBody List<MedDispatchRec> req
    ){
        logger.info("Received /calcDeliveryPath request");
        return ResponseEntity.ok(pathPlanningService.calcDeliveryPath(req));
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<GeoJsonFeatureCollection> calcDeliveryPathAsGeoJson(
            @Valid @RequestBody List<MedDispatchRec> req
    ){
        logger.info("Received /calcDeliveryPathAsGeoJson request");
        return ResponseEntity.ok(pathPlanningService
                .calcDeliveryPathAsGeoJson(req));
    }
}
