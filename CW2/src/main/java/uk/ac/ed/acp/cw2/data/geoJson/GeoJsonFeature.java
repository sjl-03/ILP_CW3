package uk.ac.ed.acp.cw2.data.geoJson;

import java.util.Map;


import lombok.Getter;
import java.util.Map;

@Getter
public class GeoJsonFeature {
    private final String type = "Feature";
    private final Geometry geometry;
    private final Map<String, Object> properties;

    public GeoJsonFeature(Geometry geometry, Map<String, Object> properties) {
        this.geometry = geometry;
        this.properties = properties;
    }
}
