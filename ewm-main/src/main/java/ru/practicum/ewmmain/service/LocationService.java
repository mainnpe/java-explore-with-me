package ru.practicum.ewmmain.service;

import ru.practicum.ewmmain.dto.location.LocationDto;
import ru.practicum.ewmmain.dto.location.NewLocationDto;

import java.util.List;

public interface LocationService {

    LocationDto create(NewLocationDto dto);

    LocationDto create(NewLocationDto dto, long userId);

    void delete(long id);

    List<LocationDto> findAll(int from, int size);

    List<LocationDto> findAll(long userId, int from, int size);

    List<LocationDto> findAll(List<Long> cityIds, int from, int size);

    List<LocationDto> findAll(long userId, List<Long> cityIds, int from, int size);

    LocationDto find(long id);

    LocationDto find(long userId, long locId);

}
