package uk.ac.ed.acp.cw2.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.client.IlpRestClient;
import uk.ac.ed.acp.cw2.data.Capabilities;
import uk.ac.ed.acp.cw2.data.Drone;
import uk.ac.ed.acp.cw2.data.Query;

import java.lang.reflect.Field;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DroneQueriesServiceImpl implements DroneQueriesService {
    private final IlpRestClient ilpRestClient;
//    private Drone drone;

    public DroneQueriesServiceImpl(IlpRestClient ilpRestClient) {
        this.ilpRestClient = ilpRestClient;
    }
    @Override
    public List<String> getDronesWithCooling(boolean state)
    {
        List<Drone> drones = ilpRestClient.getDrones();
        return drones.stream()
                .filter(drone -> drone.capability().cooling() == state)
                .map(Drone::id)
                .collect(Collectors.toList());
    }

    @Override
    public Drone getDroneDetails(String droneId)
    {
        return ilpRestClient.getDrones()
                .stream().filter(drone -> drone.id().equals(droneId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"Drone not found"));
    }

    @Override
    public List<String> queryAsPath(String attribute, String value)
    {
        List<Drone> drones = ilpRestClient.getDrones();
        return drones.stream()
                .filter(drone -> matchAttribute(drone, attribute, value))
                .map(Drone::id).toList();
    }

    @Override
    public List<String> query(List<Query> filters)
    {
        List<Drone> drones = ilpRestClient.getDrones();
        return drones.stream()
                .filter(drone -> matchAllCondition(drone, filters))
                .map(Drone::id).toList();
    }

    /**
     * Get the values in the field of Capabilities
     */
    private Object getFieldValue (Drone drone, String attribute){
        try {
            Capabilities capability = drone.capability();
            Field field = Capabilities.class.getDeclaredField(attribute);
            field.setAccessible(true);
            return field.get(capability);
        } catch (Exception e) {
            throw new IllegalArgumentException (e);
        }
    }

    private boolean matchAttribute (Drone drone, String attribute,
                                    String value){
        Object fieldValue = getFieldValue(drone, attribute);
        return switch (fieldValue) {
            case Boolean b -> b
                    .equals(Boolean.parseBoolean(value));
            case Integer i -> i
                    .equals(Integer.parseInt(value));
            case Double v -> v
                    .equals(Double.parseDouble(value));
            case null, default -> false;
        };
    }

    private boolean matchAllCondition (Drone drone, List<Query> queries){
        for (Query query : queries){
            Object fieldValue = getFieldValue(drone, query.attribute());
            if (compareAttributes(fieldValue,
                    query.value(), query.operator())){
                continue;
            }
            else  {
                return false;
            }
        }
        return true;
    }

    private boolean compareAttributes (Object fieldValue, String Value,
                                       String operator){
        if (fieldValue == null){
            return false;
        }
        else if (fieldValue instanceof Boolean){
            boolean fieldValueBoolean = (Boolean) fieldValue;
            boolean valueBoolean = Boolean.parseBoolean(Value);
            return switch (operator) {
                case "="    -> fieldValueBoolean == valueBoolean;
                case "!="   -> fieldValueBoolean != valueBoolean;
                default     -> false;
            };
        }
        else if (fieldValue instanceof Integer){
            int fieldValueInteger = (Integer) fieldValue;
            int valueInteger = Integer.parseInt(Value);
            return compareDouble(operator, fieldValueInteger, valueInteger);

        }
        else if (fieldValue instanceof Double){
            double fieldValueDouble = (Double) fieldValue;
            double valueDouble = Double.parseDouble(Value);
            return compareDouble(operator, fieldValueDouble, valueDouble);
        }
        else return false;
    }

    private boolean compareDouble (String operator, double a, double b){
        return switch (operator) {
            case "="    -> a == b;
            case "!="   -> a != b;
            case ">"    -> a > b;
            case "<"    -> a < b;
            default -> false;
        };
    }
}