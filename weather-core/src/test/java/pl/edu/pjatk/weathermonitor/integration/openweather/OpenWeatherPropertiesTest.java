package pl.edu.pjatk.weathermonitor.integration.openweather;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenWeatherPropertiesTest {

    @Test
    void settersAndGettersExposeConfiguredValues() {
        OpenWeatherProperties properties = new OpenWeatherProperties();
        properties.setBaseUrl("http://example.com");
        properties.setApiKey("api-key");
        properties.setUnits("metric");
        properties.setLanguage("pl");

        assertThat(properties.getBaseUrl()).isEqualTo("http://example.com");
        assertThat(properties.getApiKey()).isEqualTo("api-key");
        assertThat(properties.getUnits()).isEqualTo("metric");
        assertThat(properties.getLanguage()).isEqualTo("pl");
    }
}
