package pl.edu.pjatk.weathermonitor.web.rest.dto;

public record CityResponse(
        Long id,
        String name,
        String countryCode,
        Double latitude,
        Double longitude
) { }
