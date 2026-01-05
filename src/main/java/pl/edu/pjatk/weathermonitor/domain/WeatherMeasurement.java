package pl.edu.pjatk.weathermonitor.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "weather_measurements")
public class WeatherMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "temperature", nullable = false)
    private double temperature;

    @Column(name = "feels_like_temperature", nullable = false)
    private double feelsLikeTemperature;

    @Column(name = "humidity", nullable = false)
    private int humidity;

    @Column(name = "pressure", nullable = false)
    private int pressure;

    @Column(name = "wind_speed", nullable = false)
    private double windSpeed;

    @Column(name = "weather_description", length = 255, nullable = false)
    private String weatherDescription;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    protected WeatherMeasurement() {
        // for JPA
    }

    public WeatherMeasurement(
            City city,
            double temperature,
            double feelsLikeTemperature,
            int humidity,
            int pressure,
            double windSpeed,
            String weatherDescription,
            OffsetDateTime measuredAt
    ) {
        this.city = city;
        this.temperature = temperature;
        this.feelsLikeTemperature = feelsLikeTemperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.weatherDescription = weatherDescription;
        this.measuredAt = measuredAt;
    }

    public Long getId() {
        return id;
    }

    public City getCity() {
        return city;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getFeelsLikeTemperature() {
        return feelsLikeTemperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public OffsetDateTime getMeasuredAt() {
        return measuredAt;
    }
}
