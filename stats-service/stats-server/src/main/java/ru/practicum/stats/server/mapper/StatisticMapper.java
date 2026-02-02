package ru.practicum.stats.server.mapper;

import ru.practicum.stats.dto.CreateEndpointHitDto;
import ru.practicum.stats.dto.ResponseEndpointHitDto;
import ru.practicum.stats.server.model.EndpointHitEntity;

public final class StatisticMapper {

    private StatisticMapper() {
    }

    public static ResponseEndpointHitDto toDto(EndpointHitEntity hit) {
        return ResponseEndpointHitDto.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }

    public static EndpointHitEntity toEntity(CreateEndpointHitDto dto) {
        EndpointHitEntity hit = new EndpointHitEntity();
        hit.setApp(dto.getApp());
        hit.setUri(dto.getUri());
        hit.setIp(dto.getIp());
        hit.setTimestamp(dto.getTimestamp());
        return hit;
    }
}