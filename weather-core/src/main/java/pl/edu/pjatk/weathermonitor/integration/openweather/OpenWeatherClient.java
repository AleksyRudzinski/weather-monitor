package pl.edu.pjatk.weathermonitor.integration.openweather;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.edu.pjatk.weathermonitor.integration.openweather.dto.OpenWeatherCurrentResponse;

@Component
public class OpenWeatherClient {

    private final RestClient restClient;
    private final OpenWeatherProperties properties;

    public OpenWeatherClient(OpenWeatherProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public OpenWeatherCurrentResponse getCurrentWeather(double latitude, double longitude) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
                        .queryParam("appid", properties.getApiKey())
                        .queryParam("units", properties.getUnits())
                        .queryParam("lang", properties.getLanguage())
                        .build())
                .retrieve()
                .body(OpenWeatherCurrentResponse.class);
    }
}
