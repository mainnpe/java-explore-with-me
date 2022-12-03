package ru.practicum.ewmstats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.practicum.ewmstats.dto.EndpointHit;
import ru.practicum.ewmstats.dto.HitsMapper;
import ru.practicum.ewmstats.dto.ViewStatsDto;
import ru.practicum.ewmstats.model.Hit;
import ru.practicum.ewmstats.model.ViewStats;
import ru.practicum.ewmstats.storage.HitsRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final HitsRepository hitsRepository;
    private final HitsMapper hitsMapper;

    @Override
    public void hit(EndpointHit newHit) {
        Hit savedHit = hitsRepository.save(hitsMapper.toHit(newHit));
        log.info("Hit to {} has been saved", savedHit.getUri());
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        List<ViewStats> stats;
        if (unique) {
            if (CollectionUtils.isEmpty(uris)) {
                stats = hitsRepository.countDistinctIpByUri(start, end);
            } else {
                stats = hitsRepository.countDistinctIpByUri(start, end, uris);
            }
        } else {
            if (CollectionUtils.isEmpty(uris)) {
                stats = hitsRepository.countByUri(start, end);
            } else {
                stats = hitsRepository.countByUri(start, end, uris);
            }
        }
        return hitsMapper.toViewStatsDtos(stats);
    }
}
