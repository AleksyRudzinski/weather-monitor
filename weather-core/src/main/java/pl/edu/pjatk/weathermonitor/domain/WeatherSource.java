package pl.edu.pjatk.weathermonitor.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "weather_sources")
public class WeatherSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "source_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    protected WeatherSource() {
        // for JPA
    }

    public WeatherSource(String code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
