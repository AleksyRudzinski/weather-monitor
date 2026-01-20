package pl.edu.pjatk.weathermonitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.pjatk.weathermonitor.domain.City;
import pl.edu.pjatk.weathermonitor.domain.WeatherMeasurement;
import pl.edu.pjatk.weathermonitor.domain.WeatherSource;
import pl.edu.pjatk.weathermonitor.integration.openweather.OpenWeatherClient;
import pl.edu.pjatk.weathermonitor.integration.openweather.dto.OpenWeatherCurrentResponse;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.repository.WeatherMeasurementRepository;
import pl.edu.pjatk.weathermonitor.repository.WeatherSourceRepository;
import pl.edu.pjatk.weathermonitor.service.dto.WeatherMeasurementResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DefaultWeatherMeasurementService implements WeatherMeasurementService {

    private static final String OPENWEATHER_CODE = "OPENWEATHER";

    private final CityRepository cityRepository;
    private final WeatherMeasurementRepository weatherRepository;
    private final WeatherSourceRepository weatherSourceRepository;
    private final OpenWeatherClient openWeatherClient;

    @Value("${weather.measurements.history-limit:100}")
    private int historyLimit;

    public DefaultWeatherMeasurementService(
            CityRepository cityRepository,
            WeatherMeasurementRepository weatherRepository,
            WeatherSourceRepository weatherSourceRepository,
            OpenWeatherClient openWeatherClient
    ) {
        this.cityRepository = cityRepository;
        this.weatherRepository = weatherRepository;
        this.weatherSourceRepository = weatherSourceRepository;
        this.openWeatherClient = openWeatherClient;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "latest", key = "#cityId"),
            @CacheEvict(cacheNames = "history", allEntries = true)
    })
    @Transactional
    public void refreshWeatherForCity(Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "City not found: " + cityId
                ));

        WeatherSource source = weatherSourceRepository.findByCode(OPENWEATHER_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "WeatherSource OPENWEATHER not initialized"
                ));

        OpenWeatherCurrentResponse response =
                openWeatherClient.getCurrentWeather(city.getLatitude(), city.getLongitude());

        if (response == null || response.main() == null || response.wind() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "OpenWeather response missing required fields"
            );
        }

        String description = "unknown";
        if (response.weather() != null && !response.weather().isEmpty() && response.weather().get(0) != null) {
            if (response.weather().get(0).description() != null) {
                description = response.weather().get(0).description();
            }
        }

        if (response.main().temp() == null
                || response.main().feels_like() == null
                || response.main().humidity() == null
                || response.main().pressure() == null
                || response.wind().speed() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "OpenWeather response missing required measurements"
            );
        }

        WeatherMeasurement measurement = new WeatherMeasurement(
                city,
                source,
                response.main().temp(),
                response.main().feels_like(),
                response.main().humidity(),
                response.main().pressure(),
                response.wind().speed(),
                description,
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        weatherRepository.save(measurement);
        trimHistory(cityId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "latest", key = "#cityId")
    public WeatherMeasurementResponse getLatest(Long cityId) {
        ensureCityExists(cityId);

        WeatherMeasurement measurement = weatherRepository
                .findTopByCity_IdOrderByMeasuredAtDesc(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No weather measurement for city: " + cityId
                ));

        return mapToResponse(measurement);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "history", key = "#cityId + ':' + #limit")
    public List<WeatherMeasurementResponse> getHistory(Long cityId, int limit) {
        ensureCityExists(cityId);

        if (limit < 1 || limit > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 500");
        }

        return weatherRepository
                .findByCity_IdOrderByMeasuredAtDesc(cityId, PageRequest.of(0, limit))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void trimHistory(Long cityId) {
        if (historyLimit < 1) {
            return;
        }

        var measurements = weatherRepository.findByCity_IdOrderByMeasuredAtDesc(
                cityId,
                PageRequest.of(0, historyLimit + 1)
        );

        if (measurements.size() <= historyLimit) {
            return;
        }

        var idsToDelete = measurements.subList(historyLimit, measurements.size())
                .stream()
                .map(WeatherMeasurement::getId)
                .toList();

        weatherRepository.deleteByIdIn(idsToDelete);
    }

    private void ensureCityExists(Long cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found: " + cityId);
        }
    }

    private WeatherMeasurementResponse mapToResponse(WeatherMeasurement m) {
        return new WeatherMeasurementResponse(
                m.getId(),
                m.getCity().getId(),
                m.getTemperature(),
                m.getFeelsLikeTemperature(),
                m.getHumidity(),
                m.getPressure(),
                m.getWindSpeed(),
                m.getWeatherDescription(),
                m.getMeasuredAt()
        );
    }
}
