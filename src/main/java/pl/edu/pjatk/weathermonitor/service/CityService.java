package pl.edu.pjatk.weathermonitor.service;

import pl.edu.pjatk.weathermonitor.web.rest.dto.CityCreateRequest;
import pl.edu.pjatk.weathermonitor.web.rest.dto.CityResponse;

import java.util.List;

public interface CityService {

    CityResponse createCity(CityCreateRequest request);

    List<CityResponse> getAllCities();

    CityResponse getCityById(Long cityId);
}
