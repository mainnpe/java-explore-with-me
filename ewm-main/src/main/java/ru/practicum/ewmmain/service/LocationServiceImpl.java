package ru.practicum.ewmmain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmmain.dto.location.LocationDto;
import ru.practicum.ewmmain.dto.location.LocationMapper;
import ru.practicum.ewmmain.dto.location.NewLocationDto;
import ru.practicum.ewmmain.exception.EntityNotFoundException;
import ru.practicum.ewmmain.model.City;
import ru.practicum.ewmmain.model.Location;
import ru.practicum.ewmmain.storage.CityRepository;
import ru.practicum.ewmmain.storage.EventRepository;
import ru.practicum.ewmmain.storage.LocationRepository;
import ru.practicum.ewmmain.storage.UserRepository;
import ru.practicum.ewmmain.utils.CustomPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public LocationDto create(NewLocationDto dto) {
        City city = cityRepository.findById(dto.getCity()).orElseThrow(() -> {
            log.warn("City with id = {} does not exists", dto.getCity());
            throw new EntityNotFoundException("City does not exists");
        });
        Location location = locationRepository.save(
                locationMapper.toLocation(dto, city));
        return locationMapper.toLocationDto(location);
    }

    @Override
    public void delete(long id) {
        if (eventRepository.existsByLocation_Id(id)) {
            log.warn("Delete operation is forbidden. " +
                    "At least one event with location_id = {} exist", id);
        }
        locationRepository.deleteById(id);
        log.info("Location with id = {} has been deleted", id);
    }

    @Override
    public List<LocationDto> findAll(int from, int size) {
        Pageable page = CustomPageRequest.of(from, size);
        List<Location> locations = locationRepository.findAll(page).getContent();
        log.info("For conditions from = {}, size = {}: {} locations has been founded",
                from, size, locations.size());
        return locationMapper.toLocationDtos(locations);
    }

    @Override
    public List<LocationDto> findAll(List<Long> cityIds, int from, int size) {
        Pageable page = CustomPageRequest.of(from, size);
        List<Location> locations = locationRepository.findByCity_IdIn(cityIds, page);
        log.info("For conditions from = {}, size = {}: {} locations has been founded",
                from, size, locations.size());
        return locationMapper.toLocationDtos(locations);
    }

    @Override
    public LocationDto find(long id) {
        Location location = locationRepository.findById(id).orElseThrow(() -> {
            log.warn("Location with id = {} does not exist", id);
            throw new EntityNotFoundException("Location not found");
        });
        return locationMapper.toLocationDto(location);
    }

    @Override
    public LocationDto find(long userId, long locId) {
        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} does not exist", userId);
            throw new EntityNotFoundException("User does not exist");
        }
        return find(locId);
    }

    @Override
    public LocationDto create(NewLocationDto dto, long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} does not exist", userId);
            throw new EntityNotFoundException("User does not exist");
        }
        return create(dto);
    }

    @Override
    public List<LocationDto> findAll(long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} does not exist", userId);
            throw new EntityNotFoundException("User does not exist");
        }
        return findAll(from, size);
    }

    @Override
    public List<LocationDto> findAll(long userId, List<Long> cityIds, int from, int size) {
        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} does not exist", userId);
            throw new EntityNotFoundException("User does not exist");
        }
        return findAll(cityIds, from, size);
    }
}
