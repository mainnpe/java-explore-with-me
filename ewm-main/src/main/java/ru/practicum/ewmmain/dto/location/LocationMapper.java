package ru.practicum.ewmmain.dto.location;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmmain.dto.city.CityMapper;
import ru.practicum.ewmmain.model.City;
import ru.practicum.ewmmain.model.Location;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LocationMapper {
    private final CityMapper cityMapper;


    public Location toLocation(NewLocationDto dto, City city) {
        return new Location(
                dto.getName(),
                city,
                dto.getLat(),
                dto.getLon()
        );
    }

    public LocationDto toLocationDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                cityMapper.toCityDto(location.getCity())
        );
    }

    public List<LocationDto> toLocationDtos(List<Location> locations) {
        return locations.stream()
                .map(this::toLocationDto)
                .collect(Collectors.toList());
    }

}
