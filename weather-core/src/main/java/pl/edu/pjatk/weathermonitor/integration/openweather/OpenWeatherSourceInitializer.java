package pl.edu.pjatk.weathermonitor.integration.openweather;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.pjatk.weathermonitor.domain.WeatherSource;
import pl.edu.pjatk.weathermonitor.repository.WeatherSourceRepository;

@Configuration
public class OpenWeatherSourceInitializer {

    public static final String OPENWEATHER_CODE = "OPENWEATHER";

    @Bean
    ApplicationRunner initOpenWeatherSource(WeatherSourceRepository repo) {
        return args -> repo.findByCode(OPENWEATHER_CODE)
                .orElseGet(() -> repo.save(new WeatherSource(OPENWEATHER_CODE)));
    }
}
