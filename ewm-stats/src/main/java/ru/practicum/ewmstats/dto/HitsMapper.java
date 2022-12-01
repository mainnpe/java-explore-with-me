package ru.practicum.ewmstats.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewmstats.model.Hit;
import ru.practicum.ewmstats.model.ViewStats;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HitsMapper {

    public Hit toHit(EndpointHit dto) {
        return new Hit(
                dto.getApp(),
                dto.getUri(),
                dto.getIp(),
                dto.getTimestamp()
        );
    }

    public ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        return new ViewStatsDto(
                viewStats.getApp(),
                viewStats.getUri(),
                viewStats.getHits()
        );
    }

    public List<ViewStatsDto> toViewStatsDtos(List<ViewStats> viewStats) {
        return viewStats.stream()
                .map(this::toViewStatsDto)
                .collect(Collectors.toList());
    }
}
