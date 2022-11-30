package ru.practicum.ewmmain.service;

import ru.practicum.ewmmain.dto.event.*;
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

    EventFullDto find(Long eventId);

    List<EventShortDto> findAll(Long userId, int from, int size);

    List<EventShortDto> findAll(Optional<String> text, List<Long> categories, boolean paid, boolean onlyAvailable,
                                Optional<LocalDateTime> rangeStart, Optional<LocalDateTime> rangeEnd,
                                EventSortState sort, int from, int size);

    List<EventFullDto> findAll(List<Long> users, List<EventStatus> states, List<Long> categories,
                               Optional<LocalDateTime> rangeStart, Optional<LocalDateTime> rangeEnd,
                               int from, int size);


}
