package pl.edu.pjatk.weathermonitor.web.rest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.edu.pjatk.weathermonitor.service.WeatherMeasurementService;
import pl.edu.pjatk.weathermonitor.service.dto.WeatherMeasurementResponse;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/cities")
public class WeatherMeasurementRestController {

    private final WeatherMeasurementService weatherMeasurementService;

    public WeatherMeasurementRestController(WeatherMeasurementService weatherMeasurementService) {
        this.weatherMeasurementService = weatherMeasurementService;
    }

    @PostMapping("/{cityId}/weather/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refreshWeather(@PathVariable Long cityId) {
        weatherMeasurementService.refreshWeatherForCity(cityId);
    }

    @GetMapping("/{cityId}/weather/latest")
    public WeatherMeasurementResponse getLatest(@PathVariable Long cityId) {
        return weatherMeasurementService.getLatest(cityId);
    }

    @GetMapping("/{cityId}/weather/history")
    public List<WeatherMeasurementResponse> getHistory(
            @PathVariable Long cityId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit
    ) {
        return weatherMeasurementService.getHistory(cityId, limit);
    }
}
