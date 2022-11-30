package ru.practicum.ewmmain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmmain.dto.category.CategoryDto;
import ru.practicum.ewmmain.dto.category.NewCategoryDto;
import ru.practicum.ewmmain.dto.compilation.CompilationDto;
import ru.practicum.ewmmain.dto.compilation.NewCompilationDto;
import ru.practicum.ewmmain.dto.event.AdminUpdateEventDto;
import ru.practicum.ewmmain.dto.event.EventFullDto;
import ru.practicum.ewmmain.dto.user.NewUserRequest;
import ru.practicum.ewmmain.dto.user.UserDto;
import ru.practicum.ewmmain.model.event.EventStatus;
import ru.practicum.ewmmain.service.CategoryService;
import ru.practicum.ewmmain.service.CompilationService;
import ru.practicum.ewmmain.service.EventService;
import ru.practicum.ewmmain.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/admin")
@Validated
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    @GetMapping("/users")
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                  @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        return userService.findAll(ids, from, size);
    }

    @PostMapping("/users")
    public UserDto addUser(@RequestBody @Valid NewUserRequest userRequest) {
        return userService.create(userRequest);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable @Min(1) @NotNull Long userId) {
        userService.delete(userId);
    }

    @PostMapping("/categories")
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto dto) {
        return categoryService.create(dto);
    }

    @PatchMapping("/categories")
    public CategoryDto modifyCategory(@RequestBody @Valid CategoryDto dto) {
        return categoryService.update(dto);
    }

    @DeleteMapping("/categories/{catId}")
    public void deleteCategory(@PathVariable @Min(1) @NotNull Long catId) {
        categoryService.delete(catId);
    }

    @PutMapping("/events/{eventId}")
    public EventFullDto modifyEvent(@RequestBody AdminUpdateEventDto dto,
                                    @PathVariable Long eventId) {
        dto.setId(eventId);
        return eventService.modify(dto);
    }

    @PatchMapping("/events/{eventId}/publish")
    public EventFullDto publishEvent(@PathVariable @NotNull @Min(1) Long eventId) {
        return eventService.publish(eventId);
    }

    @PatchMapping("/events/{eventId}/reject")
    public EventFullDto rejectEvent(@PathVariable @NotNull @Min(1) Long eventId) {
        return eventService.cancel(eventId);
    }

    @GetMapping("/events")
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<EventStatus> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                        Optional<LocalDateTime> rangeStart,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                        Optional<LocalDateTime> rangeEnd,
                                        @RequestParam(defaultValue = "0") @Min(0) @NotNull int from,
                                        @RequestParam(defaultValue = "10") @Min(1) @NotNull int size) {
        return eventService.findAll(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PostMapping("/compilations")
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto dto) {
        return compilationService.create(dto);
    }

    @DeleteMapping("/compilations/{compId}")
    public void deleteCompilation(@PathVariable @NotNull @Min(1) Long compId) {
        compilationService.delete(compId);
    }

    @DeleteMapping("/compilations/{compId}/events/{eventId}")
    public void deleteEventFromCompilation(@PathVariable @NotNull @Min(1) Long compId,
                                           @PathVariable @NotNull @Min(1) Long eventId) {
        compilationService.deleteEvent(compId, eventId);
    }

    @PatchMapping("/compilations/{compId}/events/{eventId}")
    public void addEventToCompilation(@PathVariable @NotNull @Min(1) Long compId,
                                      @PathVariable @NotNull @Min(1) Long eventId) {
        compilationService.addEvent(compId, eventId);
    }

    @DeleteMapping("/compilations/{compId}/pin")
    public void unpinCompilation(@PathVariable @NotNull @Min(1) Long compId) {
        compilationService.unpin(compId);
    }

    @PatchMapping("/compilations/{compId}/pin")
    public void pinCompilation(@PathVariable @NotNull @Min(1) Long compId) {
        compilationService.pin(compId);
    }

}
