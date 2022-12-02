package ru.practicum.ewmmain.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmain.validation.IsAfterConstraint;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateEventDto {

    @NotNull(message = "EventId cannot be null")
    @Min(value = 1, message = "EventId cannot be less than 1")
    @JsonProperty("eventId")
    private Long id;

    @NotBlank(message = "Annotation cannot be blank")
    @Size(min = 20, max = 2000, message = "Annotation length must be between 20 and 2000")
    private String annotation;

    @Min(value = 1)
    private Long category;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 20, max = 7000, message = "Description length must be between 20 and 7000")
    private String description;

    @NotNull(message = "Event date cannot be null")
    @Future(message = "Event date must be in the future")
    @IsAfterConstraint(offsetInHours = 2)

    private LocalDateTime eventDate;

    private Boolean paid;

    private Integer participantLimit;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 120, message = "Title length must be between 3 and 120")
    private String title;

}
