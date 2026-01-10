package pl.edu.pjatk.weathermonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjatk.weathermonitor.domain.City;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.service.dto.CityCreateRequest;
import pl.edu.pjatk.weathermonitor.service.dto.CityResponse;


import java.util.List;

@Service
@Transactional
public class DefaultCityService implements CityService {

    private static final Logger log = LoggerFactory.getLogger(DefaultCityService.class);

    private final CityRepository cityRepository;
    // 1. Dodajemy serwis pogodowy
    private final WeatherMeasurementService weatherMeasurementService;

    public DefaultCityService(CityRepository cityRepository, WeatherMeasurementService weatherMeasurementService) {
        this.cityRepository = cityRepository;
        this.weatherMeasurementService = weatherMeasurementService;
    }

    @Override
    public CityResponse createCity(CityCreateRequest request) {
        City city = new City(
                request.name(),
                request.countryCode(),
                request.latitude(),
                request.longitude()
        );

        City savedCity = cityRepository.save(city);

        // 2. Od razu po dodaniu miasta, próbujemy pobrać dla niego pogodę
        // Robimy to w try-catch, żeby błąd OpenWeather nie zablokował dodania samego miasta
        try {
            weatherMeasurementService.refreshWeatherForCity(savedCity.getId());
            log.info("City created and initial weather fetched for: {}", savedCity.getName());
        } catch (Exception e) {
            log.warn("City created, but failed to fetch initial weather for: {}", savedCity.getName(), e);
        }

        return mapToResponse(savedCity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CityResponse getCityById(Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("City not found: " + cityId));

        return mapToResponse(city);
    }

    private CityResponse mapToResponse(City city) {
        return new CityResponse(
                city.getId(),
                city.getName(),
                city.getCountryCode(),
                city.getLatitude(),
                city.getLongitude()
        );
    }
}