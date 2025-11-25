package uk.ac.ed.acp.cw2.services;

import uk.ac.ed.acp.cw2.data.DeliveryPath;
import uk.ac.ed.acp.cw2.data.MedDispatchRec;
import uk.ac.ed.acp.cw2.data.geoJson.GeoJsonFeatureCollection;

import java.util.List;


public interface PathPlanningService {
    List<String> queryAvailableDrones (List<MedDispatchRec> medDispatchRecs);

    DeliveryPath calcDeliveryPath (List<MedDispatchRec> medDispatchRecs);
     GeoJsonFeatureCollection calcDeliveryPathAsGeoJson (
            List<MedDispatchRec> medDispatchRecs);
}
