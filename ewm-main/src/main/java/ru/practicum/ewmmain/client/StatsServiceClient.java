package ru.practicum.ewmmain.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewmmain.dto.stats.EndpointHit;
import ru.practicum.ewmmain.dto.stats.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsServiceClient extends BaseClient {
    public static final LocalDateTime START_DATE = LocalDateTime.of(2020, Month.JANUARY,
            1, 0, 0);
    public static final LocalDateTime END_DATE = LocalDateTime.of(2050, Month.JANUARY,
            1, 0, 0);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsServiceClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> saveHit(EndpointHit newHit) {
        return post("/hit", newHit);
    }

    public List<ViewStatsDto> getStats(@Nullable String uri) {
        StringBuilder path = new StringBuilder("/stats?start={start}&end={end}");
        Map<String, Object> parameters = new HashMap<>(
                Map.of(
                        "start", START_DATE.format(formatter),
                        "end", END_DATE.format(formatter)
                ));
        if (uri != null) {
            parameters.put("uris", uri);
            path.append("&uris={uris}");
        }
        return get(path.toString(), parameters, new ParameterizedTypeReference<List<ViewStatsDto>>() {
        });
    }


}
