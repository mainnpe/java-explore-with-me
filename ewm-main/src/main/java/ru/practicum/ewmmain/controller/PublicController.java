package ru.practicum.ewmmain.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmmain.dto.category.CategoryDto;
import ru.practicum.ewmmain.dto.compilation.CompilationDto;
import ru.practicum.ewmmain.dto.event.EventFullDto;
import ru.practicum.ewmmain.dto.event.EventShortDto;
import ru.practicum.ewmmain.dto.event.EventSortState;
import ru.practicum.ewmmain.service.CategoryService;
import ru.practicum.ewmmain.service.CompilationService;
import ru.practicum.ewmmain.service.EventService;

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
                                         @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                         @RequestParam(defaultValue = "10") @Min(1) @NotNull int size,
                                         HttpServletRequest request) {
        log.warn("!!!!!!! uri = {}, ip = {}", request.getRequestURI(), request.getRemoteAddr());
        return eventService.findAll(text, categories, paid, onlyAvailable, rangeStart, rangeEnd,
                sort, from, size, request.getRemoteAddr(), request.getRequestURI());
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


}
