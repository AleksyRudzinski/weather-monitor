package pl.edu.pjatk.weathermonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pjatk.weathermonitor.domain.WeatherSource;

import java.util.Optional;

public interface WeatherSourceRepository extends JpaRepository<WeatherSource, Long> {

    Optional<WeatherSource> findByCode(String code);
}
