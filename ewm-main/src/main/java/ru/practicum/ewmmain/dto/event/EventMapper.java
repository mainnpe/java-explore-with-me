package ru.practicum.ewmmain.dto.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmmain.dto.category.CategoryMapper;
import ru.practicum.ewmmain.dto.user.UserMapper;
import ru.practicum.ewmmain.model.Category;
import ru.practicum.ewmmain.model.User;
import ru.practicum.ewmmain.model.event.Event;
import ru.practicum.ewmmain.model.event.EventLocation;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    //categoryRepository
    //userRepository
    //stat service result

    public Event toEvent(NewEventDto dto) {
        //todo
        //get category from db
        Category category = new Category();
        category.setId(dto.getCategory());
        //todo
        //get user from db
        User user = new User();
        user.setId(dto.getUserId());

        Event.EventBuilder eventBuilder = Event.builder()
                .annotation(dto.getAnnotation())
                //todo
                .category(category)
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .location(toEventLocation(dto.getLocation()))
                .title(dto.getTitle())
                //todo
                .user(user);

        if (dto.getPaid() != null) {
            eventBuilder.paid(dto.getPaid());
        }

        if (dto.getParticipantLimit() != null) {
            eventBuilder.participantLimit(dto.getParticipantLimit());
        }

        if (dto.getRequestModeration() != null) {
            eventBuilder.requestModeration(dto.getRequestModeration());
        }

        return eventBuilder.build();
    }

    public EventShortDto toEventShortDto(Event event) {
        //todo
        //get views from stat service
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

    public List<EventShortDto> toEventShortDtos(List<Event> events) {
        return events.stream()
                .map(this::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto toEventFullDto(Event event) {
        //todo
        //get views from stat service
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

    public EventLocation toEventLocation(EventLocationDto dto) {
        return new EventLocation(dto.getLat(), dto.getLon());
    }

    public EventLocationDto toEventLocationDto(EventLocation location) {
        return new EventLocationDto(location.getLatitude(), location.getLongitude());
    }

}
