package ru.practicum.ewmmain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewmmain.client.StatsServiceClient;

@Configuration
public class WebClientConfig {

    @Value("${ewm-stats.url}")
    private String serverUrl;

    @Bean
    public StatsServiceClient statsServiceClient(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
        return new StatsServiceClient(restTemplate);
    }

}
