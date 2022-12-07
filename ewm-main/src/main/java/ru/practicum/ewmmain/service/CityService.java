package ru.practicum.ewmmain.service;

import ru.practicum.ewmmain.dto.city.CityDto;
import ru.practicum.ewmmain.dto.city.NewCityDto;

import java.util.List;

public interface CityService {

    CityDto create(NewCityDto dto);

    void delete(long id);

    List<CityDto> findAll(int from, int size);

    CityDto findById(long id);

}
