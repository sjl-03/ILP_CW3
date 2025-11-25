package uk.ac.ed.acp.cw2.data.geoJson;

import lombok.Getter;

import java.util.List;

@Getter
public class Point extends Geometry {
    private final List<Double> coordinates;

    public Point(List<Double> coordinates) {
        super("Point");
        this.coordinates = coordinates;
    }
}