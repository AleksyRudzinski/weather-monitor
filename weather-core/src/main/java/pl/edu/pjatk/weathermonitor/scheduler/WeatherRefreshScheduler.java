package pl.edu.pjatk.weathermonitor.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.pjatk.weathermonitor.domain.RefreshJob;
import pl.edu.pjatk.weathermonitor.domain.RefreshJobItem;
import pl.edu.pjatk.weathermonitor.repository.CityRepository;
import pl.edu.pjatk.weathermonitor.repository.RefreshJobItemRepository;
import pl.edu.pjatk.weathermonitor.repository.RefreshJobRepository;
import pl.edu.pjatk.weathermonitor.service.WeatherMeasurementService;

import java.time.OffsetDateTime;

@Component
@ConditionalOnProperty(
        prefix = "weather.scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class WeatherRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(WeatherRefreshScheduler.class);

    private final CityRepository cityRepository;
    private final WeatherMeasurementService weatherMeasurementService;
    private final RefreshJobRepository refreshJobRepository;
    private final RefreshJobItemRepository refreshJobItemRepository;

    public WeatherRefreshScheduler(
            CityRepository cityRepository,
            WeatherMeasurementService weatherMeasurementService,
            RefreshJobRepository refreshJobRepository,
            RefreshJobItemRepository refreshJobItemRepository
    ) {
        this.cityRepository = cityRepository;
        this.weatherMeasurementService = weatherMeasurementService;
        this.refreshJobRepository = refreshJobRepository;
        this.refreshJobItemRepository = refreshJobItemRepository;
    }

    @Scheduled(
            fixedDelayString = "${weather.scheduler.refresh-interval-ms}",
            initialDelay = 5000 // <--- TO ZMIANA: Startuj po 5 sekundach od uruchomienia
    )
    public void refreshAllCities() {
        var cities = cityRepository.findAll();

        if (cities.isEmpty()) {
            log.info("Scheduler: no cities to refresh");
            return;
        }

        // 1. Rozpoczynamy zadanie (Job)
        log.info("Scheduler: starting refresh for {} cities", cities.size());
        RefreshJob job = new RefreshJob(OffsetDateTime.now());
        job = refreshJobRepository.save(job);

        int success = 0;
        int failed = 0;

        // 2. Iterujemy po miastach
        for (var city : cities) {
            String status;
            String errorMessage = null;

            try {
                weatherMeasurementService.refreshWeatherForCity(city.getId());
                status = "SUCCESS";
                success++;
            } catch (Exception ex) {
                status = "FAILED";
                errorMessage = ex.getMessage();
                // przycinamy error, jeśli jest za długi, żeby nie wywaliło bazy (kolumna ma 500 znaków)
                if (errorMessage != null && errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }
                failed++;
                log.warn("Scheduler: refresh failed for cityId={}", city.getId(), ex);
            }

            // 3. Zapisujemy wynik dla konkretnego miasta (Item)
            RefreshJobItem item = new RefreshJobItem(
                    job,
                    city,
                    status,
                    errorMessage,
                    OffsetDateTime.now()
            );
            refreshJobItemRepository.save(item);
        }

        // 4. Kończymy zadanie (Job)
        job.markCompleted(OffsetDateTime.now());
        refreshJobRepository.save(job);

        log.info("Scheduler: done. JobId={}, success={}, failed={}", job.getId(), success, failed);
    }
}