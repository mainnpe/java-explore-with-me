package ru.practicum.ewmmain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmain.dto.category.CategoryDto;
import ru.practicum.ewmmain.dto.location.LocationDto;
import ru.practicum.ewmmain.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class EventShortDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private LocalDateTime eventDate;
    private LocationDto location;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
