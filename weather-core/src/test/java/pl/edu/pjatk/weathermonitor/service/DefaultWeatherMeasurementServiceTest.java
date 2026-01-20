package pl.edu.pjatk.weathermonitor.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.pjatk.weathermonitor.domain.City;
import pl.edu.pjatk.weathermonitor.domain.WeatherMeasurement;
import pl.edu.pjatk.weathermonitor.domain.WeatherSource;
import pl.edu.pjatk.weathermonitor.integration.openweather.OpenWeatherClient;
import pl.edu.pjatk.weathermonitor.integration.openweather.dto.OpenWeatherCurrentResponse;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.repository.WeatherMeasurementRepository;
import pl.edu.pjatk.weathermonitor.repository.WeatherSourceRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefaultWeatherMeasurementServiceTest {

    @Test
    void refreshWeatherForCityThrowsWhenCityMissing() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );

        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refreshWeatherForCity(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void refreshWeatherForCityThrowsWhenSourceMissing() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );

        City city = new City("Test", "PL", 1.0, 2.0);
        ReflectionTestUtils.setField(city, "id", 1L);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(weatherSourceRepository.findByCode("OPENWEATHER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refreshWeatherForCity(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refreshWeatherForCityThrowsOnIncompleteResponse() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );

        City city = new City("Test", "PL", 1.0, 2.0);
        ReflectionTestUtils.setField(city, "id", 1L);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(weatherSourceRepository.findByCode("OPENWEATHER"))
                .thenReturn(Optional.of(new WeatherSource("OPENWEATHER")));
        when(openWeatherClient.getCurrentWeather(1.0, 2.0))
                .thenReturn(new OpenWeatherCurrentResponse(null, null, null, "Test"));

        assertThatThrownBy(() -> service.refreshWeatherForCity(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void refreshWeatherForCitySavesAndTrimsHistory() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );
        ReflectionTestUtils.setField(service, "historyLimit", 2);

        City city = new City("Test", "PL", 1.0, 2.0);
        ReflectionTestUtils.setField(city, "id", 1L);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(weatherSourceRepository.findByCode("OPENWEATHER"))
                .thenReturn(Optional.of(new WeatherSource("OPENWEATHER")));

        var response = new OpenWeatherCurrentResponse(
                List.of(new OpenWeatherCurrentResponse.WeatherDescription("Clouds", "few clouds", "01d")),
                new OpenWeatherCurrentResponse.MainSection(10.0, 9.0, 80, 1000),
                new OpenWeatherCurrentResponse.WindSection(5.0),
                "Test"
        );
        when(openWeatherClient.getCurrentWeather(1.0, 2.0)).thenReturn(response);

        WeatherMeasurement m1 = mock(WeatherMeasurement.class);
        WeatherMeasurement m2 = mock(WeatherMeasurement.class);
        WeatherMeasurement m3 = mock(WeatherMeasurement.class);
        when(m1.getId()).thenReturn(3L);
        when(m2.getId()).thenReturn(2L);
        when(m3.getId()).thenReturn(1L);
        when(weatherRepository.findByCity_IdOrderByMeasuredAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(m1, m2, m3));

        service.refreshWeatherForCity(1L);

        verify(weatherRepository).save(any(WeatherMeasurement.class));
        verify(weatherRepository).deleteByIdIn(List.of(1L));
    }

    @Test
    void getLatestThrowsWhenCityMissing() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );

        when(cityRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.getLatest(9L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        verifyNoInteractions(weatherRepository);
    }

    @Test
    void getLatestThrowsWhenNoMeasurements() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );

        when(cityRepository.existsById(9L)).thenReturn(true);
        when(weatherRepository.findTopByCity_IdOrderByMeasuredAtDesc(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatest(9L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getLatestReturnsMappedResponse() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );

        when(cityRepository.existsById(5L)).thenReturn(true);

        City city = new City("X", "PL", 1.0, 2.0);
        ReflectionTestUtils.setField(city, "id", 5L);
        WeatherSource source = new WeatherSource("OPENWEATHER");

        WeatherMeasurement measurement = new WeatherMeasurement(
                city,
                source,
                15.5,
                14.1,
                60,
                1010,
                3.2,
                "clear",
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        ReflectionTestUtils.setField(measurement, "id", 99L);

        when(weatherRepository.findTopByCity_IdOrderByMeasuredAtDesc(5L))
                .thenReturn(Optional.of(measurement));

        var response = service.getLatest(5L);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.cityId()).isEqualTo(5L);
        assertThat(response.weatherDescription()).isEqualTo("clear");
    }

    @Test
    void getHistoryThrowsWhenLimitInvalid() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );

        when(cityRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.getHistory(1L, 0))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getHistoryReturnsMappedResults() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementRepository weatherRepository = mock(WeatherMeasurementRepository.class);
        WeatherSourceRepository weatherSourceRepository = mock(WeatherSourceRepository.class);
        OpenWeatherClient openWeatherClient = mock(OpenWeatherClient.class);
        DefaultWeatherMeasurementService service = new DefaultWeatherMeasurementService(
                cityRepository, weatherRepository, weatherSourceRepository, openWeatherClient
        );

        when(cityRepository.existsById(2L)).thenReturn(true);

        City city = new City("X", "PL", 1.0, 2.0);
        ReflectionTestUtils.setField(city, "id", 2L);
        WeatherSource source = new WeatherSource("OPENWEATHER");

        WeatherMeasurement m1 = new WeatherMeasurement(
                city, source, 10.0, 9.0, 50, 1000, 2.0, "sun", OffsetDateTime.now(ZoneOffset.UTC)
        );
        ReflectionTestUtils.setField(m1, "id", 1L);
        WeatherMeasurement m2 = new WeatherMeasurement(
                city, source, 11.0, 10.0, 55, 1001, 2.5, "rain", OffsetDateTime.now(ZoneOffset.UTC)
        );
        ReflectionTestUtils.setField(m2, "id", 2L);

        when(weatherRepository.findByCity_IdOrderByMeasuredAtDesc(eq(2L), any(PageRequest.class)))
                .thenReturn(List.of(m2, m1));

        var results = service.getHistory(2L, 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).weatherDescription()).isEqualTo("rain");
        assertThat(results.get(1).weatherDescription()).isEqualTo("sun");
    }
}
