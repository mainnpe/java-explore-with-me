package ru.practicum.ewmmain.dto.event;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmain.validation.IsAfterConstraint;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
public class NewEventDto {

    @NotBlank(message = "Annotation cannot be blank")
    @Size(min = 20, max = 2000, message = "Annotation length must be between 20 and 2000")
    private String annotation;

    @Min(value = 1)
    private Long category;

    private Long userId;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 20, max = 7000, message = "Description length must be between 20 and 7000")
    private String description;

    @NotNull(message = "Event date cannot be null")
    @Future(message = "Event date must be in the future")
    @IsAfterConstraint(offsetInHours = 2)
    private LocalDateTime eventDate;


    private Long location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 120, message = "Title length must be between 3 and 120")
    private String title;

}
