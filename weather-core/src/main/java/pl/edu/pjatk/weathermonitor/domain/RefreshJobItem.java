package pl.edu.pjatk.weathermonitor.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_job_items")
public class RefreshJobItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private RefreshJob job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // Np. "SUCCESS", "FAILED"

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "attempt_at", nullable = false)
    private OffsetDateTime attemptAt;

    protected RefreshJobItem() {
        // dla JPA
    }

    public RefreshJobItem(RefreshJob job, City city, String status, String errorMessage, OffsetDateTime attemptAt) {
        this.job = job;
        this.city = city;
        this.status = status;
        this.errorMessage = errorMessage;
        this.attemptAt = attemptAt;
    }

    // Gettery
    public Long getId() { return id; }
    public RefreshJob getJob() { return job; }
    public City getCity() { return city; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public OffsetDateTime getAttemptAt() { return attemptAt; }
}