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

import java.time.Duration;
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
            initialDelay = 5000 // Startuj po 5 sekundach od uruchomienia
    )
    public void refreshAllCities() {
        var cities = cityRepository.findAll();

        if (cities.isEmpty()) {
            log.info("Scheduler: no cities to refresh");
            return;
        }

        // 1) Start joba (czas startu + zapis Job)
        OffsetDateTime jobStart = OffsetDateTime.now();
        log.info("Scheduler: starting refresh for {} cities", cities.size());

        RefreshJob job = new RefreshJob(jobStart);
        job = refreshJobRepository.save(job);

        int success = 0;
        int failed = 0;

        // 2) Iterujemy po miastach
        for (var city : cities) {
            String status;
            String errorMessage = null;

            OffsetDateTime attemptAt = OffsetDateTime.now();

            try {
                weatherMeasurementService.refreshWeatherForCity(city.getId());
                status = "SUCCESS";
                success++;
            } catch (Exception ex) {
                status = "FAILED";
                errorMessage = ex.getMessage();

                // przycinamy error, jeśli jest za długi (kolumna ma 500 znaków)
                if (errorMessage != null && errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }

                failed++;
                log.warn("Scheduler: refresh failed for cityId={}", city.getId(), ex);
            }

            // 3) Zapisujemy wynik dla konkretnego miasta (Item)
            RefreshJobItem item = new RefreshJobItem(
                    job,
                    city,
                    status,
                    errorMessage,
                    attemptAt
            );
            refreshJobItemRepository.save(item);
        }

        // 4) Koniec joba: status + endTime + duration
        OffsetDateTime jobEnd = OffsetDateTime.now();

        if (failed > 0) job.markFailed(jobEnd);
        else job.markCompleted(jobEnd);

        refreshJobRepository.save(job);

        long durationMs = Duration.between(jobStart, jobEnd).toMillis();
        log.info("Scheduler: done. JobId={}, success={}, failed={}, durationMs={}",
                job.getId(), success, failed, durationMs);
    }
}
