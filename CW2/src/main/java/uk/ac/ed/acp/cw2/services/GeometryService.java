package uk.ac.ed.acp.cw2.services;

import uk.ac.ed.acp.cw2.data.Position;
import uk.ac.ed.acp.cw2.data.Region;


public interface GeometryService {
    double euclidianDistance(Position p1, Position p2);
    boolean isCloseTo(Position p1, Position p2);
    Position nextPosition(Position start, double angleDeg);
    boolean isInRegion(Position p, Region region);
    boolean pointOnEdge(Position p, Position a, Position b);
}
