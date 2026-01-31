package ru.practicum.stats.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.CreateEndpointHitDto;
import ru.practicum.stats.dto.ResponseEndpointHitDto;
import ru.practicum.stats.server.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StatisticMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResponseEndpointHitDto toDto(EndpointHitEntity hit) {
        return ResponseEndpointHitDto.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp().format(FORMATTER))
                .build();
    }

    public EndpointHitEntity toEntity(CreateEndpointHitDto dto) {
        EndpointHitEntity hit = new EndpointHitEntity();
        hit.setApp(dto.getApp());
        hit.setUri(dto.getUri());
        hit.setIp(dto.getIp());
        hit.setTimestamp(LocalDateTime.parse(dto.getTimestamp(), FORMATTER));
        return hit;
    }
}