package ru.practicum.stats.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.CreateEndpointHitDto;
import ru.practicum.stats.dto.ResponseEndpointHitDto;
import ru.practicum.stats.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${stats.server.url}") String serverUrl
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.serverUrl = serverUrl;
    }

    public ResponseEndpointHitDto sendHit(HttpServletRequest request) {
        CreateEndpointHitDto hitDto = CreateEndpointHitDto.builder()
                .app(serverUrl)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        ResponseEntity<ResponseEndpointHitDto> response =
                restTemplate.postForEntity(
                        serverUrl + "/hit",
                        hitDto,
                        ResponseEndpointHitDto.class
                );

        return response.getBody();
    }

    public List<ViewStats> getStats(String start, String end, List<String> uris, boolean unique) {
        String encodedStart = URLEncoder.encode(start, StandardCharsets.UTF_8);
        String encodedEnd = URLEncoder.encode(end, StandardCharsets.UTF_8);

        StringBuilder urlBuilder = new StringBuilder(serverUrl)
                .append("/stats?start=").append(encodedStart)
                .append("&end=").append(encodedEnd);

        if (uris != null && !uris.isEmpty()) {
            String encodedUris = uris.stream()
                    .map(uri -> URLEncoder.encode(uri, StandardCharsets.UTF_8))
                    .collect(Collectors.joining(","));
            urlBuilder.append("&uris=").append(encodedUris);
        }

        urlBuilder.append("&unique=").append(unique);

        ResponseEntity<ViewStats[]> response =
                restTemplate.getForEntity(urlBuilder.toString(), ViewStats[].class);

        ViewStats[] body = response.getBody();
        return body != null ? Arrays.asList(body) : List.of();
    }
}