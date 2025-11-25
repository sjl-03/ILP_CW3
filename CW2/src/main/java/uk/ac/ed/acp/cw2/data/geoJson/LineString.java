package uk.ac.ed.acp.cw2.data.geoJson;

import lombok.Getter;

import java.util.List;

@Getter
public class LineString extends Geometry {
    private final List<List<Double>> coordinates;

    public LineString(List<List<Double>> coordinates) {
        super("LineString");
        this.coordinates = coordinates;
    }
}
