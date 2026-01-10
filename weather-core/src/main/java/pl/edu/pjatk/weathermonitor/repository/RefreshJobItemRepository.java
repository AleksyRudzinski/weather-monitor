package pl.edu.pjatk.weathermonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pjatk.weathermonitor.domain.RefreshJobItem;

public interface RefreshJobItemRepository extends JpaRepository<RefreshJobItem, Long> {
}