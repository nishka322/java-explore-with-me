package ru.practicum.stats.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResponseEndpointHitDto {
    private Long id;
    private String app;
    private String uri;
    private String ip;
    private LocalDateTime timestamp;
}