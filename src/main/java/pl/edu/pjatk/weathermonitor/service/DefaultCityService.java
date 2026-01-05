package pl.edu.pjatk.weathermonitor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjatk.weathermonitor.domain.City;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.web.rest.dto.CityCreateRequest;
import pl.edu.pjatk.weathermonitor.web.rest.dto.CityResponse;

import java.util.List;

@Service
@Transactional
public class DefaultCityService implements CityService {

    private final CityRepository cityRepository;

    public DefaultCityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
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
