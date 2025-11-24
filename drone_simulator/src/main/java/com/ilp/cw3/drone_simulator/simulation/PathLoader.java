package com.ilp.cw3.drone_simulator.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ilp.cw3.drone_simulator.model.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class PathLoader {
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public DeliveryPath loadPathJson(){
        try{
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("path.json");
            return objectMapper
                    .readValue(inputStream, DeliveryPath.class);
        } catch (Exception e){
            throw new RuntimeException("Fail to load path",e);
        }
    }

    public DroneDetail loadDroneDetailJson(){
        try{
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("droneDetail.json");

            return objectMapper
                    .readValue(inputStream, DroneDetail.class);
        } catch (Exception e){
            throw new RuntimeException("Fail to load path",e);
        }
    }

}
