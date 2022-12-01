package ru.practicum.ewmstats.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewmstats.model.Hit;
import ru.practicum.ewmstats.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HitsRepository extends JpaRepository<Hit, Long> {

    @Query("select new ru.practicum.ewmstats.model.ViewStats(h.app, h.uri, count(h.id)) " +
            "from Hit h " +
            "where h.timestamp >= :start " +
            "and h.timestamp <= :end " +
            "group by h.app, h.uri")
    List<ViewStats> countByUri(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ewmstats.model.ViewStats(h.app, h.uri, count(h.id)) " +
            "from Hit h " +
            "where h.timestamp >= :start " +
            "and h.timestamp <= :end " +
            "and h.uri in :uris " +
            "group by h.app, h.uri")
    List<ViewStats> countByUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ewmstats.model.ViewStats(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit h " +
            "where h.timestamp >= :start " +
            "and h.timestamp <= :end " +
            "group by h.app, h.uri")
    List<ViewStats> countDistinctIpByUri(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ewmstats.model.ViewStats(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit h " +
            "where h.timestamp >= :start " +
            "and h.timestamp <= :end " +
            "and h.uri in :uris " +
            "group by h.app, h.uri")
    List<ViewStats> countDistinctIpByUri(LocalDateTime start, LocalDateTime end, List<String> uris);

}
