package pl.edu.pjatk.weathermonitor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjatk.weathermonitor.domain.City;
import pl.edu.pjatk.weathermonitor.domain.WeatherMeasurement;
import pl.edu.pjatk.weathermonitor.integration.openweather.OpenWeatherClient;
import pl.edu.pjatk.weathermonitor.integration.openweather.dto.OpenWeatherCurrentResponse;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.repository.WeatherMeasurementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@Transactional
public class DefaultWeatherMeasurementService
        implements WeatherMeasurementService {

    private final CityRepository cityRepository;
    private final WeatherMeasurementRepository weatherRepository;
    private final OpenWeatherClient openWeatherClient;

    public DefaultWeatherMeasurementService(
            CityRepository cityRepository,
            WeatherMeasurementRepository weatherRepository,
            OpenWeatherClient openWeatherClient
    ) {
        this.cityRepository = cityRepository;
        this.weatherRepository = weatherRepository;
        this.openWeatherClient = openWeatherClient;
    }

    @Override
    public void refreshWeatherForCity(Long cityId) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "City not found: " + cityId
                ));

        OpenWeatherCurrentResponse response =
                openWeatherClient.getCurrentWeather(
                        city.getLatitude(),
                        city.getLongitude()
                );

        WeatherMeasurement measurement = new WeatherMeasurement(
                city,
                response.main().temp(),
                response.main().feels_like(),
                response.main().humidity(),
                response.main().pressure(),
                response.wind().speed(),
                response.weather().get(0).description(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        weatherRepository.save(measurement);
    }
}
