package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServiceControllerIntegrationTest {
    private static final double EPS = 1e-12;
    HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void health_isUp() {
        var result = rest.getForEntity("http://localhost:"+port+
                "/actuator/health", Map.class);
        assertThat(result.getStatusCode()== HttpStatus.OK).isTrue();
        assertThat(result.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void uid_returnsPlainId() {
        var result = rest.getForEntity("http://localhost:"+port+
                "/api/v1/uid", String.class);
        assertThat(result.getStatusCode()== HttpStatus.OK).isTrue();
        assertThat(result.getBody()).matches("s2559435");
    }

    @Test
    void misspelt_endpoint() {
        var result = rest.getForEntity("http://localhost:"+port+
                "/api/v1/uidd", String.class);
        assertThat(result.getStatusCode()== HttpStatus.OK).isFalse();
    }

    @Test
    void distanceTo_extraFieldsIgnored() {
        String input = """
        {
          "position1":{"lng":-3.192473,"lat":55.946233},
          "position2":{"lng":-3.192473,"lat":55.942617},
          "ignored":"xyz"
        }
        """;
        headers.setContentType(MediaType.APPLICATION_JSON);
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/distanceTo",
                new HttpEntity<>(input, headers), Double.class);
        assertThat(result.getStatusCode()== HttpStatus.OK).isTrue();
        assertThat(result.getBody()).isNotNull();
        System.out.println(result.getBody());
        assertThat(result.getBody()).isCloseTo(0.003616,
                within(EPS));
    }

    @Test
    void distanceTo_missingField() {
        String input = """
        {"position1":{"lng":-3.192473,"lat":55.946233}}
        """;
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/distanceTo",
                new HttpEntity<>(input, headers), String.class);
        assertThat(result.getStatusCode() == HttpStatus.BAD_REQUEST).isTrue();
    }

    @Test
    void isCloseTo_boundary() { // must be strictly < 0.00015
        
        headers.setContentType(MediaType.APPLICATION_JSON);

        String input = """
        {"position1":{"lng":0,"lat":0},"position2":{"lng":0.00015,"lat":0}}
        """;
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/isCloseTo",
                new HttpEntity<>(input, headers), Boolean.class);
        assertThat(result.getStatusCode()== HttpStatus.OK).isTrue();
        assertThat(result.getBody()).isFalse();
    }

    @Test
    void isCloseTo_inside() {
        
        headers.setContentType(MediaType.APPLICATION_JSON);

        String input = """
        {"position1":{"lng":0,"lat":0},"position2":{"lng":0.000149999,"lat":0}}
        """;
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/isCloseTo",
                new HttpEntity<>(input, headers), Boolean.class);
        assertThat(result.getStatusCode()== HttpStatus.OK).isTrue();
        assertThat(result.getBody()).isTrue();
    }

    @Test
    void nextPosition_valid_45d() {

        headers.setContentType(MediaType.APPLICATION_JSON);

        String input = """
        {"start":{"lng":-3.192473,"lat":55.946233},"angle":45}
        """;
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/nextPosition",
                new HttpEntity<>(input, headers), Map.class);
        Double lat = Double.parseDouble(result.getBody().get("lat").toString());
        double lng = Double.parseDouble(result.getBody().get("lng").toString());
        System.out.println(lat + ", " + lng);
        double step = 0.00015*Math.cos(Math.toRadians(45));
        assertThat(lat).isCloseTo(55.946233 + step, within(EPS));
        assertThat(lng).isCloseTo(-3.192473 + step, within(EPS));
    }

    @Test
    void nextPosition_valid_225d() {

        headers.setContentType(MediaType.APPLICATION_JSON);

        String input = """
        {"start":{"lng":-3.192473,"lat":55.946233},"angle":22.5}
        """;
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/nextPosition",
                new HttpEntity<>(input, headers), Map.class);
        Double lat = Double.parseDouble(result.getBody().get("lat").toString());
        double lng = Double.parseDouble(result.getBody().get("lng").toString());
        System.out.println(lat + ", " + lng);
        double stepx = 0.00015*Math.cos(Math.toRadians(22.5));
        double stepy = 0.00015*Math.sin(Math.toRadians(22.5));
        assertThat(lat).isCloseTo(55.946233 + stepy, within(EPS));
        assertThat(lng).isCloseTo(-3.192473 + stepx, within(EPS));
    }

    @Test
    void nextPosition_invalid_angle() {
        
        headers.setContentType(MediaType.APPLICATION_JSON);

        String input = """
        {"start":{"lng":-3.192473,"lat":55.946233},"angle":40}
        """;
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/nextPosition",
                new HttpEntity<>(input, headers), String.class);
        assertThat(result.getStatusCode() == HttpStatus.BAD_REQUEST).isTrue();
    }


    @Test
    void isInRegion_validRec() {
        
        headers.setContentType(MediaType.APPLICATION_JSON);

        String input = """
        {"position":{"lng":-3.19,"lat":55.944},
         "region":{"name":"central",
                   "vertices":[
                     {"lng":-3.192473,"lat":55.946233},
                     {"lng":-3.192473,"lat":55.942617},
                     {"lng":-3.184319,"lat":55.942617},
                     {"lng":-3.184319,"lat":55.946233},
                     {"lng":-3.192473,"lat":55.946233}
                   ]}}
        """;
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/isInRegion",
                new HttpEntity<>(input, headers), Boolean.class);
        assertThat(result.getStatusCode()== HttpStatus.OK).isTrue();
        assertThat(result.getBody()).isTrue();
    }

    @Test
    void isInRegion_openPolygon() {
        
        headers.setContentType(MediaType.APPLICATION_JSON);

        String input = """
        {"position":{"lng":0,"lat":0},
         "region":{"name":"bad",
                   "vertices":[
                     {"lng":0,"lat":0},
                     {"lng":1,"lat":0},
                     {"lng":1,"lat":1}
                   ]}}
        """;
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/isInRegion",
                new HttpEntity<>(input, headers), String.class);
        assertThat(result.getStatusCode() == HttpStatus.BAD_REQUEST).isTrue();
    }

    @Test
    void malformedJson() {
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        var input = "{this is not valid json";
        var result = rest.postForEntity("http://localhost:"+port+
                        "/api/v1/distanceTo",
                new HttpEntity<>(input, headers), String.class);
        assertThat(result.getStatusCode() == HttpStatus.BAD_REQUEST).isTrue();
    }
}
