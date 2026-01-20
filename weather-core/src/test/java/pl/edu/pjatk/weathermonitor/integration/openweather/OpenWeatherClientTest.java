package pl.edu.pjatk.weathermonitor.integration.openweather;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pl.edu.pjatk.weathermonitor.integration.openweather.dto.OpenWeatherCurrentResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenWeatherClientTest {

    @Test
    void getCurrentWeatherBuildsRequestAndParsesResponse() {
        OpenWeatherProperties properties = new OpenWeatherProperties();
        properties.setBaseUrl("http://localhost");
        properties.setApiKey("test-key");
        properties.setUnits("metric");
        properties.setLanguage("pl");

        OpenWeatherClient client = new OpenWeatherClient(properties);
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        ReflectionTestUtils.setField(client, "restClient", restClient);
        server.expect(requestTo("http://localhost/weather?lat=52.1&lon=21.0&appid=test-key&units=metric&lang=pl"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "weather": [
                            { "main": "Clouds", "description": "scattered clouds", "icon": "03d" }
                          ],
                          "main": { "temp": 12.3, "feels_like": 11.0, "humidity": 65, "pressure": 1012 },
                          "wind": { "speed": 4.2 },
                          "name": "Warsaw"
                        }
                        """, MediaType.APPLICATION_JSON));

        OpenWeatherCurrentResponse response = client.getCurrentWeather(52.1, 21.0);

        server.verify();
        assertThat(response.name()).isEqualTo("Warsaw");
        assertThat(response.weather()).hasSize(1);
        assertThat(response.weather().get(0).description()).isEqualTo("scattered clouds");
        assertThat(response.main().temp()).isEqualTo(12.3);
        assertThat(response.wind().speed()).isEqualTo(4.2);
    }
}
