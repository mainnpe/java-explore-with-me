package ru.practicum.ewmmain.dto.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminUpdateEventDto {
    private Long id;
    private String annotation;
    private Long category;
    private String description;
    private LocalDateTime eventDate;
    private Long location;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String title;
}
