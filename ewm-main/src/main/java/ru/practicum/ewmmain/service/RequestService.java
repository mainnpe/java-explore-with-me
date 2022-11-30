package ru.practicum.ewmmain.service;

import ru.practicum.ewmmain.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto create(long userId, long eventId);

    List<ParticipationRequestDto> findAll(long userId);

    List<ParticipationRequestDto> findAll(long userId, long eventId);

    ParticipationRequestDto cancel(long userId, long requestId);

    ParticipationRequestDto confirm(long userId, long eventId, long requestId);

    ParticipationRequestDto reject(long userId, long eventId, long requestId);

    void cancelRedundantRequests(long eventId);


}
