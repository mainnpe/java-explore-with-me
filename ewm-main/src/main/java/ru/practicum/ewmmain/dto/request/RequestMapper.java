package ru.practicum.ewmmain.dto.request;

import org.springframework.stereotype.Component;
import ru.practicum.ewmmain.model.request.Request;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestMapper {

    public ParticipationRequestDto toRequestDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getRequester().getId(),
                request.getEvent().getId(),
                request.getStatus(),
                request.getCreated()
        );
    }

    public List<ParticipationRequestDto> toRequestDtos(List<Request> requests) {
        return requests.stream()
                .map(this::toRequestDto)
                .collect(Collectors.toList());
    }
}