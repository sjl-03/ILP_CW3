package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;

import uk.ac.ed.acp.cw2.data.Position;
import uk.ac.ed.acp.cw2.data.Region;
import uk.ac.ed.acp.cw2.services.GeometryService;
import uk.ac.ed.acp.cw2.services.GeometryServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

class GeometryServiceImplTest {

    private final GeometryService geo = new GeometryServiceImpl();
    private static final double EPS = 1e-12;

    @Test
    void euclidianDistance_basic() {
        var a = new Position(-3.192473, 55.946233);
        var b = new Position(-3.192473, 55.942617);
        double d = geo.euclidianDistance(a, b);
        assertThat(d).isCloseTo(0.003616, within(EPS));
    }

    @Test
    void isCloseTo_strictlyLess_than_0_00015() {
        var a = new Position(0.0, 0.0);
        var exactly = new Position(0.00015, 0.0);
        var slightlyLess = new Position(0.0001499999999, 0.0);
        assertThat(geo.isCloseTo(a, exactly)).isFalse();  // must be < 0.00015
        assertThat(geo.isCloseTo(a, slightlyLess)).isTrue();
    }

    @Test
    void nextPosition_movesBy_0_00015_in_bearing() {
        var start = new Position(-3.192473, 55.946233);
        var angle = 45.0;
        var next = geo.nextPosition(start, angle);
        double step = geo.euclidianDistance(start, next);
        assertThat(step).isCloseTo(0.00015, within(EPS));
    }

    @Test
    void isInRegion_strictInside_true() {
        var square = new Region("sq", List.of(
                new Position(0.0,0.0), new Position(0.0,1.0),
                new Position(1.0,1.0), new Position(1.0,0.0),
                new Position(0.0,0.0)
        ));
        assertThat(geo.isInRegion(new Position(0.5, 0.5), square)).isTrue();
    }

    @Test
    void isInRegion_onBorder_false_when_strictInside_required() {
        var square = new Region("sq", List.of(
                new Position(0.0,0.0), new Position(0.0,1.0),
                new Position(1.0,1.0), new Position(1.0,0.0),
                new Position(0.0,0.0)
        ));
        assertThat(geo.isInRegion(new Position(1.0, 0.5), square)).isTrue();
    }
}
