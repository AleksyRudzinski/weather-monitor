package pl.edu.pjatk.weathermonitor.integration.openweather;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import pl.edu.pjatk.weathermonitor.domain.WeatherSource;
import pl.edu.pjatk.weathermonitor.repository.WeatherSourceRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenWeatherSourceInitializerTest {

    @Test
    void initOpenWeatherSourceSkipsSaveWhenPresent() throws Exception {
        WeatherSourceRepository repository = mock(WeatherSourceRepository.class);
        when(repository.findByCode(OpenWeatherSourceInitializer.OPENWEATHER_CODE))
                .thenReturn(Optional.of(new WeatherSource(OpenWeatherSourceInitializer.OPENWEATHER_CODE)));

        var runner = new OpenWeatherSourceInitializer().initOpenWeatherSource(repository);
        runner.run(mock(ApplicationArguments.class));

        verify(repository).findByCode(OpenWeatherSourceInitializer.OPENWEATHER_CODE);
        org.mockito.Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    void initOpenWeatherSourceCreatesWhenMissing() throws Exception {
        WeatherSourceRepository repository = mock(WeatherSourceRepository.class);
        when(repository.findByCode(OpenWeatherSourceInitializer.OPENWEATHER_CODE))
                .thenReturn(Optional.empty());

        var runner = new OpenWeatherSourceInitializer().initOpenWeatherSource(repository);
        runner.run(mock(ApplicationArguments.class));

        verify(repository).save(any(WeatherSource.class));
    }
}
