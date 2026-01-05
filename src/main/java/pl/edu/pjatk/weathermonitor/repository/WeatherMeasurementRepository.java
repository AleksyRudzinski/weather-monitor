package pl.edu.pjatk.weathermonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pjatk.weathermonitor.domain.WeatherMeasurement;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import java.util.List;


public interface WeatherMeasurementRepository extends JpaRepository<WeatherMeasurement, Long> {

    List<WeatherMeasurement> findByCity_IdOrderByMeasuredAtDesc(Long cityId, Pageable pageable);
    Optional<WeatherMeasurement> findTopByCity_IdOrderByMeasuredAtDesc(Long cityId);
}
