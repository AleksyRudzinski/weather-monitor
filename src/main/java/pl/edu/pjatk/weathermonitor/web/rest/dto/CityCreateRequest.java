package pl.edu.pjatk.weathermonitor.web.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CityCreateRequest(

        @NotBlank
        @Size(max = 120)
        String name,

        @Size(min = 2, max = 2)
        String countryCode,

        @NotNull
        @Min(-90)
        @Max(90)
        Double latitude,

        @NotNull
        @Min(-180)
        @Max(180)
        Double longitude
) { }
