package uk.ac.ed.acp.cw2.services;

import uk.ac.ed.acp.cw2.data.Drone;
import uk.ac.ed.acp.cw2.data.Query;

import java.util.List;

public interface DroneQueriesService {
    /**
     * dronesWithCooling/{state} (GET)
     * Return a [] of drone-ids which support cooling (state is true) or
     * not (false)
     */
    List<String> getDronesWithCooling(boolean state);

    /**
     * Return the JSON object for a single drone with the given id.
     */
    Drone getDroneDetails(String droneId);

    /**
     * return an [] of drone-ids which have their attribute with the given
     * value as path-variables in the URL.
     */
    List<String> queryAsPath(String attribute, String value);

    /**
     * return an [] of drone-ids which have their attribute(s) with the given
     * value(s) according to the passed in POST-data.
     */
    List<String> query(List<Query> filters);
}
