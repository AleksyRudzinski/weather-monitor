package pl.edu.pjatk.weathermonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WeatherMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherMonitorApplication.class, args);
    }
}
