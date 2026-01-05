package pl.edu.pjatk.weathermonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pjatk.weathermonitor.domain.City;

public interface CityRepository extends JpaRepository<City, Long> {
}
