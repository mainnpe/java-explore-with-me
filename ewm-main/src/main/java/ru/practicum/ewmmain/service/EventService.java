package ru.practicum.ewmmain.service;

import org.springframework.lang.Nullable;
import ru.practicum.ewmmain.dto.event.*;
import ru.practicum.ewmmain.dto.stats.ViewStatsDto;
import ru.practicum.ewmmain.model.event.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventService {

    EventFullDto create(NewEventDto newEventDto);

    EventFullDto modify(AdminUpdateEventDto updateEventDto);

    EventFullDto modify(UpdateEventDto updateEventDto, Long userId);

    EventFullDto publish(Long eventId);

    EventFullDto cancel(Long eventId);

    EventFullDto cancel(Long eventId, Long userId);

    EventFullDto find(Long eventId, Long userId);

    EventFullDto find(Long eventId, String ip, String uri);

    List<EventShortDto> findAll(Long userId, int from, int size);

    List<EventShortDto> findAll(Optional<String> text, List<Long> categories, Optional<Boolean> paid, boolean onlyAvailable,
                                Optional<LocalDateTime> rangeStart, Optional<LocalDateTime> rangeEnd,
                                EventSortState sort, List<Long> cities, List<Long> locations, Double lat, Double lon,
                                Double r, int from, int size, String ip, String uri);

    List<EventFullDto> findAll(List<Long> users, List<EventStatus> states, List<Long> categories,
                               Optional<LocalDateTime> rangeStart, Optional<LocalDateTime> rangeEnd,
                               int from, int size);

    void saveEndpointHit(String ip, String uri);

    List<ViewStatsDto> getStats(@Nullable String uri);

    List<ViewStatsDto> getStats();


}
