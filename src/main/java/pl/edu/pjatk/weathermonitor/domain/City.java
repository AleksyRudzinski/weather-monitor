package pl.edu.pjatk.weathermonitor.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    protected City() {
        // Required by JPA
    }

    public City(String name, String countryCode, Double latitude, Double longitude) {
        this.name = name;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
