package pl.edu.pjatk.weathermonitor.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pl.edu.pjatk.weathermonitor.service.WeatherMeasurementService;
import pl.edu.pjatk.weathermonitor.domain.WeatherMeasurement;
import pl.edu.pjatk.weathermonitor.repository.WeatherMeasurementRepository;
import pl.edu.pjatk.weathermonitor.web.rest.dto.WeatherMeasurementResponse;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class WeatherMeasurementRestController {

    private final WeatherMeasurementService weatherMeasurementService;
    private final WeatherMeasurementRepository weatherMeasurementRepository;

    public WeatherMeasurementRestController(
            WeatherMeasurementService weatherMeasurementService,
            WeatherMeasurementRepository weatherMeasurementRepository
    ) {
        this.weatherMeasurementService = weatherMeasurementService;
        this.weatherMeasurementRepository = weatherMeasurementRepository;
    }

    @PostMapping("/{cityId}/weather/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refreshWeather(@PathVariable Long cityId) {
        weatherMeasurementService.refreshWeatherForCity(cityId);
    }

    @GetMapping("/{cityId}/weather/latest")
    public WeatherMeasurementResponse getLatest(@PathVariable Long cityId) {
        WeatherMeasurement measurement = weatherMeasurementRepository
                .findTopByCity_IdOrderByMeasuredAtDesc(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No weather measurement for city: " + cityId
                ));

        return new WeatherMeasurementResponse(
                measurement.getId(),
                measurement.getCity().getId(),
                measurement.getTemperature(),
                measurement.getFeelsLikeTemperature(),
                measurement.getHumidity(),
                measurement.getPressure(),
                measurement.getWindSpeed(),
                measurement.getWeatherDescription(),
                measurement.getMeasuredAt()
        );
    }
    @GetMapping("/{cityId}/weather/history")
    public List<WeatherMeasurementResponse> getHistory(
            @PathVariable Long cityId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        if (limit < 1 || limit > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 500");
        }

        return weatherMeasurementRepository
                .findByCity_IdOrderByMeasuredAtDesc(cityId, PageRequest.of(0, limit))
                .stream()
                .map(m -> new WeatherMeasurementResponse(
                        m.getId(),
                        m.getCity().getId(),
                        m.getTemperature(),
                        m.getFeelsLikeTemperature(),
                        m.getHumidity(),
                        m.getPressure(),
                        m.getWindSpeed(),
                        m.getWeatherDescription(),
                        m.getMeasuredAt()
                ))
                .toList();
    }
}

