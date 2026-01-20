package pl.edu.pjatk.weathermonitor.scheduler;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import pl.edu.pjatk.weathermonitor.domain.City;
import pl.edu.pjatk.weathermonitor.domain.RefreshJob;
import pl.edu.pjatk.weathermonitor.domain.RefreshJobItem;
import pl.edu.pjatk.weathermonitor.domain.RefreshJobStatus;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.repository.RefreshJobItemRepository;
import pl.edu.pjatk.weathermonitor.repository.RefreshJobRepository;
import pl.edu.pjatk.weathermonitor.service.WeatherMeasurementService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WeatherRefreshSchedulerTest {

    @Test
    void refreshAllCitiesReturnsEarlyWhenNoCities() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementService weatherMeasurementService = mock(WeatherMeasurementService.class);
        RefreshJobRepository refreshJobRepository = mock(RefreshJobRepository.class);
        RefreshJobItemRepository refreshJobItemRepository = mock(RefreshJobItemRepository.class);
        WeatherRefreshScheduler scheduler = new WeatherRefreshScheduler(
                cityRepository, weatherMeasurementService, refreshJobRepository, refreshJobItemRepository
        );

        when(cityRepository.findAll()).thenReturn(List.of());

        scheduler.refreshAllCities();

        verifyNoInteractions(weatherMeasurementService, refreshJobRepository, refreshJobItemRepository);
    }

    @Test
    void refreshAllCitiesMarksFailedWhenAnyCityFails() {
        CityRepository cityRepository = mock(CityRepository.class);
        WeatherMeasurementService weatherMeasurementService = mock(WeatherMeasurementService.class);
        RefreshJobRepository refreshJobRepository = mock(RefreshJobRepository.class);
        RefreshJobItemRepository refreshJobItemRepository = mock(RefreshJobItemRepository.class);
        WeatherRefreshScheduler scheduler = new WeatherRefreshScheduler(
                cityRepository, weatherMeasurementService, refreshJobRepository, refreshJobItemRepository
        );

        City city1 = new City("A", "PL", 1.0, 2.0);
        City city2 = new City("B", "PL", 3.0, 4.0);
        ReflectionTestUtils.setField(city1, "id", 1L);
        ReflectionTestUtils.setField(city2, "id", 2L);

        when(cityRepository.findAll()).thenReturn(List.of(city1, city2));
        when(refreshJobRepository.save(any(RefreshJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new RuntimeException("boom")).when(weatherMeasurementService).refreshWeatherForCity(2L);

        scheduler.refreshAllCities();

        ArgumentCaptor<RefreshJob> jobCaptor = ArgumentCaptor.forClass(RefreshJob.class);
        verify(refreshJobRepository, org.mockito.Mockito.times(2)).save(jobCaptor.capture());
        var savedJobs = jobCaptor.getAllValues();
        assertThat(savedJobs).hasSize(2);
        assertThat(savedJobs.get(savedJobs.size() - 1).getStatus()).isEqualTo(RefreshJobStatus.FAILED);

        ArgumentCaptor<RefreshJobItem> itemCaptor = ArgumentCaptor.forClass(RefreshJobItem.class);
        verify(refreshJobItemRepository, org.mockito.Mockito.times(2)).save(itemCaptor.capture());
        assertThat(itemCaptor.getAllValues()).hasSize(2);
        assertThat(itemCaptor.getAllValues().stream().anyMatch(i -> "FAILED".equals(i.getStatus()))).isTrue();
        assertThat(itemCaptor.getAllValues().stream().anyMatch(i -> "SUCCESS".equals(i.getStatus()))).isTrue();
    }
}
