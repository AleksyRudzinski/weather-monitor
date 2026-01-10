package pl.edu.pjatk.weathermonitor.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_jobs")
public class RefreshJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefreshJobStatus status;

    protected RefreshJob() {
    }

    public RefreshJob(OffsetDateTime startTime) {
        this.startTime = startTime;
        this.status = RefreshJobStatus.RUNNING;
    }

    public void markCompleted(OffsetDateTime endTime) {
        this.status = RefreshJobStatus.COMPLETED;
        this.endTime = endTime;
    }

    public void markFailed(OffsetDateTime endTime) {
        this.status = RefreshJobStatus.FAILED;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public RefreshJobStatus getStatus() { return status; }
}