package pl.edu.pjatk.weathermonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pjatk.weathermonitor.domain.RefreshJob;
import java.util.List;

public interface RefreshJobRepository extends JpaRepository<RefreshJob, Long> {
    List<RefreshJob> findTop20ByOrderByStartTimeDesc();
}