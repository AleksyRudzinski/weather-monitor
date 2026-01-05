package pl.edu.pjatk.weathermonitor.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.service.WeatherMeasurementService;

@Component
@ConditionalOnProperty(
        prefix = "weather.scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class WeatherRefreshScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(WeatherRefreshScheduler.class);

    private final CityRepository cityRepository;
    private final WeatherMeasurementService weatherMeasurementService;

    public WeatherRefreshScheduler(
            CityRepository cityRepository,
            WeatherMeasurementService weatherMeasurementService
    ) {
        this.cityRepository = cityRepository;
        this.weatherMeasurementService = weatherMeasurementService;
    }

    @Scheduled(
            fixedDelayString = "${weather.scheduler.refresh-interval-ms}",
            initialDelayString = "${weather.scheduler.refresh-interval-ms}"
    )
    public void refreshAllCities() {

        var cities = cityRepository.findAll();

        if (cities.isEmpty()) {
            log.info("Scheduler: no cities to refresh");
            return;
        }

        log.info("Scheduler: refreshing weather for {} cities", cities.size());

        int success = 0;
        int failed = 0;

        for (var city : cities) {
            try {
                weatherMeasurementService.refreshWeatherForCity(city.getId());
                success++;
            } catch (Exception ex) {
                failed++;
                log.warn("Scheduler: refresh failed for cityId={}", city.getId(), ex);
            }
        }

        log.info("Scheduler: done. success={}, failed={}", success, failed);
    }
}
