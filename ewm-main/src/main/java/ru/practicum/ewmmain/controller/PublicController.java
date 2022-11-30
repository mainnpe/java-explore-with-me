package ru.practicum.ewmmain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
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
    public CategoryDto getCategory(@PathVariable @Min(1) @NotNull Long catId) {
        return categoryService.findById(catId);
    }

    @GetMapping("events/{id}")
    public EventFullDto getEvent(@PathVariable @Min(1) @NotNull Long id) {
        //todo add HttpRequest to Stats service
        return eventService.find(id);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEvents(@RequestParam Optional<String> text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam @NotNull Boolean paid,
                                         @RequestParam(defaultValue = "false") @NotNull Boolean onlyAvailable,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         Optional<LocalDateTime> rangeStart,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         Optional<LocalDateTime> rangeEnd,
                                         @RequestParam(defaultValue = "EVENT_DATE") EventSortState sort,
                                         @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                         @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        return eventService.findAll(text, categories, paid, onlyAvailable,
                rangeStart, rangeEnd, sort, from, size);
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
    public CompilationDto getCompilation(@PathVariable @NotNull @Min(1) Long compId) {
        return compilationService.find(compId);
    }


}
