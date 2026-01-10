package pl.edu.pjatk.weathermonitor.web.mvc.dto;

import java.time.OffsetDateTime;

public record CityDashboardItem(
        Long cityId,
        String name,
        String countryCode,
        Double latitude,
        Double longitude,
        Double temperature,
        Double feelsLikeTemperature,
        Integer humidity,
        Double windSpeed,
        String weatherDescription,
        OffsetDateTime measuredAt
) { }
