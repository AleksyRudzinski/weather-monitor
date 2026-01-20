package pl.edu.pjatk.weathermonitor.web.mvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.edu.pjatk.weathermonitor.service.CityService;
import pl.edu.pjatk.weathermonitor.service.WeatherMeasurementService;
import pl.edu.pjatk.weathermonitor.web.mvc.dto.CityDashboardItem;

@Controller
public class WeatherViewController {

    private final CityService cityService;
    private final WeatherMeasurementService weatherMeasurementService;

    @Value("${weather.measurements.history-limit:100}")
    private int historyLimit;

    @Value("${weather.scheduler.refresh-interval-ms:600000}")
    private long refreshInterval;

    public WeatherViewController(
            CityService cityService,
            WeatherMeasurementService weatherMeasurementService
    ) {
        this.cityService = cityService;
        this.weatherMeasurementService = weatherMeasurementService;
    }

    @GetMapping("/")
    public String getDashboard(Model model) {
        var cities = cityService.getAllCities();

        var items = cities.stream()
                .map(city -> {
                    try {
                        var latest = weatherMeasurementService.getLatest(city.id());
                        return new CityDashboardItem(
                                city.id(), city.name(), city.countryCode(), city.latitude(), city.longitude(),
                                latest.temperature(),
                                latest.feelsLikeTemperature(),
                                latest.humidity(),
                                latest.windSpeed(),
                                latest.weatherDescription(),
                                latest.measuredAt()
                        );
                    } catch (Exception e) {
                        // brak pomiaru -> karta bez danych
                        return new CityDashboardItem(
                                city.id(), city.name(), city.countryCode(), city.latitude(), city.longitude(),
                                null, null, null, null, null, null
                        );
                    }
                })
                .toList();

        model.addAttribute("cities", items);
        model.addAttribute("citiesCount", items.size());
        model.addAttribute("historyLimit", historyLimit);
        model.addAttribute("refreshInterval", refreshInterval);

        return "index";
    }

    @GetMapping("/ui/cities/{cityId}")
    public String getCityDetails(@PathVariable Long cityId, Model model) {
        var city = cityService.getCityById(cityId);

        // latest może nie istnieć -> wtedy nie wywalamy całej strony
        Object latest;
        try {
            latest = weatherMeasurementService.getLatest(cityId);
        } catch (Exception ignored) {
            latest = null;
        }

        var history = weatherMeasurementService.getHistory(cityId, historyLimit);

        model.addAttribute("city", city);
        model.addAttribute("latest", latest);
        model.addAttribute("history", history);
        model.addAttribute("historyLimit", historyLimit);

        return "city-details";
    }

    // refresh z DASHBOARDU -> wracamy na "/"
    @PostMapping("/ui/cities/{cityId}/refresh")
    public String refreshCityFromDashboard(@PathVariable Long cityId, RedirectAttributes ra) {
        try {
            weatherMeasurementService.refreshWeatherForCity(cityId);
            ra.addFlashAttribute("toastSuccess", "Odświeżono pogodę dla miasta ID: " + cityId);
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Nie udało się odświeżyć (ID: " + cityId + ")");
        }
        return "redirect:/";
    }

    // refresh ze SZCZEGÓŁÓW -> wracamy na "/ui/cities/{id}"
    @PostMapping("/ui/cities/{cityId}/refresh/details")
    public String refreshCityFromDetails(@PathVariable Long cityId, RedirectAttributes ra) {
        try {
            weatherMeasurementService.refreshWeatherForCity(cityId);
            ra.addFlashAttribute("toastSuccess", "Odświeżono pogodę dla miasta ID: " + cityId);
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Nie udało się odświeżyć (ID: " + cityId + ")");
        }
        return "redirect:/ui/cities/" + cityId;
    }
}
