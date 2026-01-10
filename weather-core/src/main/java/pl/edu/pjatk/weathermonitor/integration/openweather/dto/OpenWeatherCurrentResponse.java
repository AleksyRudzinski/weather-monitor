package pl.edu.pjatk.weathermonitor.integration.openweather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherCurrentResponse(
        List<WeatherDescription> weather,
        MainSection main,
        WindSection wind,
        String name
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherDescription(
            String main,
            String description,
            String icon
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MainSection(
            Double temp,
            Double feels_like,
            Integer humidity,
            Integer pressure
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WindSection(
            Double speed
    ) { }
}
