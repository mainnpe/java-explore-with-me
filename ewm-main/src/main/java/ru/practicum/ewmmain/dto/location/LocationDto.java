package ru.practicum.ewmmain.dto.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmain.dto.city.CityDto;

@Getter
@Setter
@AllArgsConstructor
public class LocationDto {
    private Long id;
    private String name;
    private Double lat;
    private Double lon;
    private CityDto city;
}
