package uk.ac.ed.acp.cw2.data.geoJson;

import lombok.Getter;

import java.util.List;

@Getter
public class Polygon extends Geometry {
    private final List<List<List<Double>>> coordinates;

    public Polygon(List<List<List<Double>>> coordinates) {
        super("Polygon");
        this.coordinates = coordinates;
    }
}
