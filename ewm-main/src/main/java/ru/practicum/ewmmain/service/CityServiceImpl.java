package ru.practicum.ewmmain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmmain.dto.city.CityDto;
import ru.practicum.ewmmain.dto.city.CityMapper;
import ru.practicum.ewmmain.dto.city.NewCityDto;
import ru.practicum.ewmmain.exception.EntityNotFoundException;
import ru.practicum.ewmmain.model.City;
import ru.practicum.ewmmain.storage.CityRepository;
import ru.practicum.ewmmain.storage.LocationRepository;
import ru.practicum.ewmmain.utils.CustomPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;
    private final LocationRepository locationRepository;
    private final CityMapper cityMapper;

    @Override
    public CityDto create(NewCityDto dto) {
        City city = cityRepository.save(cityMapper.toCity(dto));
        log.info("City with id = {} has been created", city.getId());
        return cityMapper.toCityDto(city);
    }

    @Override
    public void delete(long id) {
        if (locationRepository.existsByCity_Id(id)) {
            log.warn("Delete operation is forbidden. " +
                    "At least one location with city_id = {} exist", id);
        }
        cityRepository.deleteById(id);
        log.info("City with id = {} has been deleted", id);
    }

    @Override
    public List<CityDto> findAll(int from, int size) {
        Pageable page = CustomPageRequest.of(from, size);
        List<City> cities = cityRepository.findAll(page).getContent();
        log.info("For conditions from = {}, size = {}: {} cities has been founded",
                from, size, cities.size());
        return cityMapper.toCityDtos(cities);
    }

    @Override
    public CityDto findById(long id) {
        City city = cityRepository.findById(id).orElseThrow(() -> {
            log.warn("City with id = {} does not exist", id);
            throw new EntityNotFoundException("City not found");
        });
        return cityMapper.toCityDto(city);
    }
}
