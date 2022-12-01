package ru.practicum.ewmstats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class EndpointHit {

    @NotNull(message = "App field cannot be null")
    private String app;

    @NotNull(message = "Uri field cannot be null")
    private String uri;

    @NotNull(message = "Ip field cannot be null")
    private String ip;

    @NotNull(message = "Timestamp field cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
