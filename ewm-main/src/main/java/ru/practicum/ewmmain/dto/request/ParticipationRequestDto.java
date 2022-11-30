package ru.practicum.ewmmain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmain.model.request.RequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ParticipationRequestDto {
    private Long id;
    private Long requester;
    private Long event;
    private RequestStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.[S]")
    private LocalDateTime created;
}
