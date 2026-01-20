package pl.edu.pjatk.weathermonitor.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.pjatk.weathermonitor.domain.City;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.service.dto.CityCreateRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCityServiceTest {

    @Test
    void createCityReturnsResponseEvenWhenRefreshFails() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementService weatherMeasurementService = mock(WeatherMeasurementService.class);
        DefaultCityService service = new DefaultCityService(cityRepository, weatherMeasurementService);

        when(cityRepository.save(any())).thenAnswer(invocation -> {
            City city = invocation.getArgument(0);
            ReflectionTestUtils.setField(city, "id", 10L);
            return city;
        });
        doThrow(new RuntimeException("boom"))
                .when(weatherMeasurementService)
                .refreshWeatherForCity(10L);

        var response = service.createCity(new CityCreateRequest("Warsaw", "PL", 52.23, 21.01));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Warsaw");
    }

    @Test
    void createCityTriggersInitialRefreshWhenPossible() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementService weatherMeasurementService = mock(WeatherMeasurementService.class);
        DefaultCityService service = new DefaultCityService(cityRepository, weatherMeasurementService);

        when(cityRepository.save(any())).thenAnswer(invocation -> {
            City city = invocation.getArgument(0);
            ReflectionTestUtils.setField(city, "id", 7L);
            return city;
        });

        service.createCity(new CityCreateRequest("Gdansk", "PL", 54.36, 18.64));

        verify(weatherMeasurementService).refreshWeatherForCity(7L);
    }

    @Test
    void getCityByIdThrowsNotFound() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementService weatherMeasurementService = mock(WeatherMeasurementService.class);
        DefaultCityService service = new DefaultCityService(cityRepository, weatherMeasurementService);

        when(cityRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCityById(123L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
