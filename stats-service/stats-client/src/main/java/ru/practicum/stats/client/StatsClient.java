package ru.practicum.stats.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.CreateEndpointHitDto;
import ru.practicum.stats.dto.ResponseEndpointHitDto;
import ru.practicum.stats.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl = "http://localhost:9090";

    public StatsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Используем DTO вместо Entity
    public ResponseEndpointHitDto sendHit(CreateEndpointHitDto hitDto) {
        ResponseEntity<ResponseEndpointHitDto> response =
                restTemplate.postForEntity(serverUrl + "/hit", hitDto, ResponseEndpointHitDto.class);
        return response.getBody();
    }

    public List<ViewStats> getStats(String start, String end, List<String> uris, boolean unique) {
        StringBuilder url = new StringBuilder(serverUrl + "/stats?start=")
                .append(URLEncoder.encode(start, StandardCharsets.UTF_8))
                .append("&end=")
                .append(URLEncoder.encode(end, StandardCharsets.UTF_8));

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                url.append("&uris=").append(URLEncoder.encode(uri, StandardCharsets.UTF_8));
            }
        }

        url.append("&unique=").append(unique);

        ResponseEntity<ViewStats[]> response =
                restTemplate.getForEntity(url.toString(), ViewStats[].class);

        return Arrays.asList(response.getBody());
    }
}