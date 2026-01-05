package pl.edu.pjatk.weathermonitor.web.rest.dto;

import java.time.OffsetDateTime;

public record WeatherMeasurementResponse(
        Long id,
        Long cityId,
        double temperature,
        double feelsLikeTemperature,
        int humidity,
        int pressure,
        double windSpeed,
        String weatherDescription,
        OffsetDateTime measuredAt
) {
}
