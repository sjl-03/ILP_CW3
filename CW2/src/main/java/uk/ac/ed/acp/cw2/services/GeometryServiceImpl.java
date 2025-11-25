package uk.ac.ed.acp.cw2.services;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.data.Position;
import uk.ac.ed.acp.cw2.data.Region;

import java.util.List;


@Service
public class GeometryServiceImpl implements GeometryService {
    private static final double STEP = 0.00015;
    private static final double CLOSE_THRESHOLD = 0.00015;
    private static final double ANGLE_CONSTRAINT = 22.5;

    private static void requireValidAngle(double angle) {
        if ((angle < 0) || (angle >= 360) || (angle % ANGLE_CONSTRAINT != 0))
            throw new IllegalArgumentException(
                    "Angle must be a multiple of 22.5 degrees.");
    }

    private static void validatePosition(Position p) {
        if (p.lat() < -90 || p.lat() > 90 || p.lng() < -180 || p.lng() > 180) {
            throw new IllegalArgumentException(
                    "Invalid coordinates: out of range");
        }
    }

    // distanceTo (POST)
    @Override
    public double euclidianDistance(Position a, Position b) {
        validatePosition(a);
        validatePosition(b);
        return Math.sqrt(Math.pow(a.lat()- b.lat(), 2)
                + Math.pow(a.lng()- b.lng(), 2));
    }

    // isCloseTo (POST)
    @Override
    public boolean isCloseTo(Position a, Position b) {
        validatePosition(a);
        validatePosition(b);
        return euclidianDistance(a, b) < CLOSE_THRESHOLD;
    }

    // nextPosition (POST)
    @Override
    public Position nextPosition(Position startPos, double angle) {
        validatePosition(startPos);
        requireValidAngle(angle);
        double angleRad = Math.toRadians(angle);
        double dx = STEP * Math.cos(angleRad);
        double dy = STEP * Math.sin(angleRad);
        return new Position(startPos.lng() + dx,
                startPos.lat() + dy);
    }

    // isInRegion (POST)
    @Override
    public boolean isInRegion(Position pos, Region region) {
        validatePosition(pos);
        boolean inRegion = false;
        List<Position> vertices = region.vertices();
        for (int i = 0;  i < vertices.size() - 1; ++i) {
            Position ver1 = vertices.get(i);
            Position ver2 = vertices.get(i+1);

            if (pointOnEdge(pos, ver1, ver2)) {
                return true;
            }
            if (crossEdge(pos, ver1, ver2)){
                inRegion = !inRegion;
            }
        }

        return inRegion;
    }

    private boolean crossEdge(Position point, Position v1, Position v2){
        double xp = point.lng();
        double yp = point.lat();
        double x1 = v1.lng();
        double y1 = v1.lat();
        double x2 = v2.lng();
        double y2 = v2.lat();
        return ((yp < y1) != (yp < y2)) &&
                (xp < x2 + (yp-y2)*((x1-x2)/(y1-y2)) );
    }

    @Override
    public boolean pointOnEdge(Position p, Position a, Position b) {
        // Check if ap and ab vector points in the same direction
        double cross = (p.lat() - a.lat()) * (b.lng() - a.lng())
                - (p.lng() - a.lng()) * (b.lat() - a.lat());
        if (Math.abs(cross) != 0) return false;

        double minX = Math.min(a.lng(), b.lng());
        double maxX = Math.max(a.lng(), b.lng());
        double minY = Math.min(a.lat(), b.lat());
        double maxY = Math.max(a.lat(), b.lat());

        return p.lng() >= minX && p.lng() <= maxX &&
                p.lat() >= minY && p.lat() <= maxY;
    }

}
