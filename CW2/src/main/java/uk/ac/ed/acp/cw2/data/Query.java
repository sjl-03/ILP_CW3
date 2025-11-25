package uk.ac.ed.acp.cw2.data;

public record Query(
        String attribute,
        String operator,
        String value
) {
}
