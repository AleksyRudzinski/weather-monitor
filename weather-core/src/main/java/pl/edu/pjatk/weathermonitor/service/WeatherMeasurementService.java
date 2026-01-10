package pl.edu.pjatk.weathermonitor.service;

import pl.edu.pjatk.weathermonitor.service.dto.WeatherMeasurementResponse;

import java.util.List;

public interface WeatherMeasurementService {

    void refreshWeatherForCity(Long cityId);

    WeatherMeasurementResponse getLatest(Long cityId);

    List<WeatherMeasurementResponse> getHistory(Long cityId, int limit);
}
