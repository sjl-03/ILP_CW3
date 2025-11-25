package uk.ac.ed.acp.cw2.data.geoJson;

import lombok.Getter;

@Getter
public abstract class Geometry {
    protected String type;

    public Geometry(String type) {
        this.type = type;
    }


}
