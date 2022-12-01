package ru.practicum.ewmstats.service;

import ru.practicum.ewmstats.dto.EndpointHit;
import ru.practicum.ewmstats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void hit(EndpointHit newHit);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                List<String> uris, Boolean unique);
}
