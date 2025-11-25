package uk.ac.ed.acp.cw2.data.geoJson;

import lombok.Getter;

import java.util.List;

@Getter
public class GeoJsonFeatureCollection {
    private final String type = "FeatureCollection";
    private final List<GeoJsonFeature> features;

    public GeoJsonFeatureCollection(List<GeoJsonFeature> features) {
        this.features = features;
    }
}
