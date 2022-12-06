package ru.practicum.ewmmain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmmain.dto.event.EventFullDto;
import ru.practicum.ewmmain.dto.event.EventShortDto;
import ru.practicum.ewmmain.dto.event.NewEventDto;
import ru.practicum.ewmmain.dto.event.UpdateEventDto;
import ru.practicum.ewmmain.dto.location.LocationDto;
import ru.practicum.ewmmain.dto.location.NewLocationDto;
import ru.practicum.ewmmain.dto.request.ParticipationRequestDto;
import ru.practicum.ewmmain.service.EventService;
import ru.practicum.ewmmain.service.LocationService;
import ru.practicum.ewmmain.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Validated
@RequiredArgsConstructor
public class PrivateController {
    private final EventService eventService;
    private final RequestService requestService;
    private final LocationService locationService;

    @PostMapping("/{userId}/events")
    public EventFullDto addEvent(@RequestBody @Valid NewEventDto newEventDto,
                                 @PathVariable @NotNull Long userId) {
        newEventDto.setUserId(userId);
        return eventService.create(newEventDto);
    }

    @PatchMapping("/{userId}/events")
    public EventFullDto modifyEvent(@RequestBody @Valid UpdateEventDto updateEventDto,
                                    @PathVariable @NotNull Long userId) {
        return eventService.modify(updateEventDto, userId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto cancelEvent(@PathVariable @NotNull Long eventId,
                                    @PathVariable @NotNull Long userId) {
        return eventService.cancel(eventId, userId);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEvent(@PathVariable @NotNull Long eventId,
                                 @PathVariable @NotNull Long userId) {
        return eventService.find(eventId, userId);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getEvents(@PathVariable @NotNull Long userId,
                                         @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                         @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        return eventService.findAll(userId, from, size);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable @NotNull Long userId) {
        return requestService.findAll(userId);
    }

    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto addRequest(@PathVariable @NotNull Long userId,
                                              @RequestParam @NotNull Long eventId) {
        return requestService.create(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @NotNull Long userId,
                                                 @PathVariable @NotNull Long requestId) {
        return requestService.cancel(userId, requestId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable @NotNull Long userId,
                                                            @PathVariable @NotNull Long eventId) {
        return requestService.findAll(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto confirmRequest(@PathVariable @NotNull Long userId,
                                                  @PathVariable @NotNull Long eventId,
                                                  @PathVariable @NotNull Long reqId) {
        ParticipationRequestDto dto = requestService.confirm(userId, eventId, reqId);
        requestService.cancelRedundantRequests(eventId);
        return dto;
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto rejectRequest(@PathVariable @NotNull Long userId,
                                                 @PathVariable @NotNull Long eventId,
                                                 @PathVariable @NotNull Long reqId) {
        return requestService.reject(userId, eventId, reqId);
    }

    @PostMapping("/{userId}/locations")
    public LocationDto addLocation(@RequestBody @Valid NewLocationDto dto,
                                   @PathVariable @NotNull Long userId) {
        return locationService.create(dto, userId);
    }

    @GetMapping("/{userId}/locations")
    public List<LocationDto> getLocations(@PathVariable @NotNull Long userId,
                                          @RequestParam(required = false) List<Long> cities,
                                          @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                          @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        if (CollectionUtils.isEmpty(cities)) {
            return locationService.findAll(userId, from, size);
        }
        return locationService.findAll(userId, cities, from, size);
    }

    @GetMapping("/{userId}/locations/{locId}")
    public LocationDto getLocation(@PathVariable @NotNull Long userId,
                                   @PathVariable @NotNull Long locId) {
        return locationService.find(userId, locId);
    }

}
