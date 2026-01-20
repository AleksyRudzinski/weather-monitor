package pl.edu.pjatk.weathermonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pjatk.weathermonitor.domain.RefreshJobItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RefreshJobItemRepository extends JpaRepository<RefreshJobItem, Long> {

    @Query("""
        select i from RefreshJobItem i
        join fetch i.job j
        join fetch i.city c
        where j.id in :jobIds
        order by i.attemptAt desc
    """)
    List<RefreshJobItem> findAllForJobs(@Param("jobIds") List<Long> jobIds);
}
