package pl.edu.pjatk.weathermonitor.service.dto;

public record CityResponse(
        Long id,
        String name,
        String countryCode,
        Double latitude,
        Double longitude
) { }
