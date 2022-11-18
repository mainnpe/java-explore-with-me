package ru.practicum.ewmmain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventLocationDto {
    private Double lat;
    private Double lon;
}
