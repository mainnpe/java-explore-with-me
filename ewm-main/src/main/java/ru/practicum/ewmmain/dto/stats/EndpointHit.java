package ru.practicum.ewmmain.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class EndpointHit {
    private String app;

    private String uri;

    private String ip;


    private LocalDateTime timestamp;
}
