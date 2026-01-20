package pl.edu.pjatk.weathermonitor.integration.openweather.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenWeatherCurrentResponseTest {

    @Test
    void deserializesExpectedFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String json = """
                {
                  "weather": [
                    { "main": "Clear", "description": "clear sky", "icon": "01d" }
                  ],
                  "main": { "temp": 18.5, "feels_like": 18.0, "humidity": 55, "pressure": 1018 },
                  "wind": { "speed": 2.1 },
                  "name": "Gdansk",
                  "extra": "ignored"
                }
                """;

        OpenWeatherCurrentResponse response = mapper.readValue(json, OpenWeatherCurrentResponse.class);

        assertThat(response.name()).isEqualTo("Gdansk");
        assertThat(response.weather()).hasSize(1);
        assertThat(response.weather().get(0).main()).isEqualTo("Clear");
        assertThat(response.main().humidity()).isEqualTo(55);
        assertThat(response.wind().speed()).isEqualTo(2.1);
    }
}
