package ru.practicum.ewmmain.dto.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import ru.practicum.ewmmain.dto.category.CategoryMapper;
import ru.practicum.ewmmain.dto.stats.ViewStatsDto;
import ru.practicum.ewmmain.dto.user.UserMapper;
import ru.practicum.ewmmain.model.Category;
import ru.practicum.ewmmain.model.User;
import ru.practicum.ewmmain.model.event.Event;
import ru.practicum.ewmmain.model.event.EventLocation;
import ru.practicum.ewmmain.model.event.EventStatus;
import ru.practicum.ewmmain.storage.CategoryRepository;
import ru.practicum.ewmmain.storage.UserRepository;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    public Event toEvent(NewEventDto dto) {
        Optional<Category> category = categoryRepository.findById(dto.getCategory());
        Optional<User> user = userRepository.findById(dto.getUserId());

        Event.EventBuilder eventBuilder = Event.builder()
                .annotation(dto.getAnnotation())
                .category(category.orElse(null))
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .location(toEventLocation(dto.getLocation()))
                .title(dto.getTitle())
                .user(user.orElse(null))
                .state(EventStatus.PENDING)
                .confirmedRequests(0);

        if (dto.getPaid() != null) {
            eventBuilder.paid(dto.getPaid());
        }

        if (dto.getParticipantLimit() != null) {
            eventBuilder.participantLimit(dto.getParticipantLimit());
        } else {
            eventBuilder.participantLimit(0);
        }

        if (dto.getRequestModeration() != null) {
            eventBuilder.requestModeration(dto.getRequestModeration());
        }

        return eventBuilder.build();
    }


    public EventShortDto toEventShortDto(Event event) {


        Long views = 0L;
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate(),
                userMapper.toUserShortDto(event.getUser()),
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    public EventShortDto toEventShortDto(Event event, List<ViewStatsDto> views) {
        EventShortDto dto = toEventShortDto(event);
        Long hits = getViews(views, event.getId());
        dto.setViews(hits);
        return dto;
    }

    public List<EventShortDto> toEventShortDtos(List<Event> events) {
        return events.stream()
                .map(this::toEventShortDto)
                .collect(Collectors.toList());
    }

    public List<EventShortDto> toEventShortDtos(List<Event> events, final List<ViewStatsDto> views) {
        return events.stream()
                .map(x -> toEventShortDto(x, views))
                .collect(Collectors.toList());
    }

    public EventFullDto toEventFullDto(Event event) {


        Long views = 0L;
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                userMapper.toUserShortDto(event.getUser()),
                toEventLocationDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views
        );
    }

    public EventFullDto toEventFullDto(Event event, List<ViewStatsDto> views) {
        EventFullDto dto = toEventFullDto(event);
        Long hits = getViews(views, event.getId());
        dto.setViews(hits);
        return dto;
    }

    private Long getViews(List<ViewStatsDto> views, Long id) {
        String uri = String.format("/events/%s", id);
        Long hits = 0L;
        Optional<ViewStatsDto> statsOpt = views.stream()
                .filter(x -> x.getUri().equals(uri))
                .findAny();

        if (statsOpt.isPresent()) {
            hits = statsOpt.get().getHits();
        }

        return hits;
    }

    public List<EventFullDto> toEventFullDtos(List<Event> events) {
        return events.stream()
                .map(this::toEventFullDto)
                .collect(Collectors.toList());
    }

    public EventLocation toEventLocation(EventLocationDto dto) {
        return new EventLocation(dto.getLat(), dto.getLon());
    }

    public EventLocationDto toEventLocationDto(EventLocation location) {
        return new EventLocationDto(location.getLatitude(), location.getLongitude());
    }

    public <T> Event updateEvent(T updateDto, Event event, Category category) {
        Field[] fields = updateDto.getClass().getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            Field fieldToUpdate = ReflectionUtils.findField(Event.class,
                    field.getName());
            try {
                field.setAccessible(true);
                Object value = (!field.getName().equals("category")) ?
                        field.get(updateDto) : category;
                if (fieldToUpdate != null && value != null) {
                    fieldToUpdate.setAccessible(true);
                    ReflectionUtils.setField(fieldToUpdate, event, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format("Access error for Field %s during " +
                        "entity update", field), e);
            }
        });
        return event;
    }

}
