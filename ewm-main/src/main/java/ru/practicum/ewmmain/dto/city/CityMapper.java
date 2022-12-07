package ru.practicum.ewmmain.dto.city;

import org.springframework.stereotype.Component;
import ru.practicum.ewmmain.model.City;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CityMapper {

    public City toCity(NewCityDto dto) {
        return new City(dto.getName());
    }

    public CityDto toCityDto(City city) {
        return new CityDto(city.getId(), city.getName());
    }

    public List<CityDto> toCityDtos(List<City> cities) {
        return cities.stream()
                .map(this::toCityDto)
                .collect(Collectors.toList());
    }

}
