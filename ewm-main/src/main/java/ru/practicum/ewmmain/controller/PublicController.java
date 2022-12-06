package ru.practicum.ewmmain.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmmain.dto.category.CategoryDto;
import ru.practicum.ewmmain.dto.city.CityDto;
import ru.practicum.ewmmain.dto.compilation.CompilationDto;
import ru.practicum.ewmmain.dto.event.EventFullDto;
import ru.practicum.ewmmain.dto.event.EventShortDto;
import ru.practicum.ewmmain.dto.event.EventSortState;
import ru.practicum.ewmmain.dto.location.LocationDto;
import ru.practicum.ewmmain.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class PublicController {
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;
    private final CityService cityService;
    private final LocationService locationService;

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                           @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        return categoryService.findAll(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategory(@PathVariable @NotNull Long catId) {
        return categoryService.findById(catId);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEvent(@PathVariable @NotNull Long id, HttpServletRequest request) {

        return eventService.find(id, request.getRemoteAddr(), request.getRequestURI());
    }

    @GetMapping("/events")
    public List<EventShortDto> getEvents(@RequestParam Optional<String> text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam Optional<Boolean> paid,
                                         @RequestParam(defaultValue = "false") @NotNull Boolean onlyAvailable,
                                         @RequestParam Optional<LocalDateTime> rangeStart,
                                         @RequestParam Optional<LocalDateTime> rangeEnd,
                                         @RequestParam(defaultValue = "EVENT_DATE") EventSortState sort,
                                         @RequestParam(required = false) List<Long> cities,
                                         @RequestParam(required = false) List<Long> locations,
                                         @RequestParam(required = false) Double x,
                                         @RequestParam(required = false) Double y,
                                         @RequestParam(required = false) Double r,
                                         @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                         @RequestParam(defaultValue = "10") @Min(1) @NotNull int size,
                                         HttpServletRequest request) {
        return eventService.findAll(text, categories, paid, onlyAvailable, rangeStart, rangeEnd,
                sort, cities, locations, x, y, r, from, size, request.getRemoteAddr(), request.getRequestURI());
    }


    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                                @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        if (pinned == null) {
            return compilationService.findAll(from, size);
        }
        return compilationService.findAll(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilation(@PathVariable @NotNull Long compId) {
        return compilationService.find(compId);
    }

    @GetMapping("/cities")
    public List<CityDto> getCities(@RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                   @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        return cityService.findAll(from, size);
    }

    @GetMapping("/locations")
    public List<LocationDto> getLocations(@RequestParam(required = false) List<Long> cities,
                                          @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                          @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        if (CollectionUtils.isEmpty(cities)) {
            return locationService.findAll(from, size);
        }
        return locationService.findAll(cities, from, size);
    }

    @GetMapping("/locations/{locId}")
    public LocationDto getLocation(@PathVariable @NotNull Long locId) {
        return locationService.find(locId);
    }

}
