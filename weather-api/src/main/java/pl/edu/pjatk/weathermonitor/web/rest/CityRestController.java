package pl.edu.pjatk.weathermonitor.web.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.pjatk.weathermonitor.service.CityService;
import pl.edu.pjatk.weathermonitor.service.dto.CityCreateRequest;
import pl.edu.pjatk.weathermonitor.service.dto.CityResponse;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityRestController {

    private final CityService cityService;

    public CityRestController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse createCity(@Valid @RequestBody CityCreateRequest request) {
        return cityService.createCity(request);
    }

    @GetMapping
    public List<CityResponse> getAllCities() {
        return cityService.getAllCities();
    }

    @GetMapping("/{cityId}")
    public CityResponse getCityById(@PathVariable Long cityId) {
        return cityService.getCityById(cityId);
    }
    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

}
