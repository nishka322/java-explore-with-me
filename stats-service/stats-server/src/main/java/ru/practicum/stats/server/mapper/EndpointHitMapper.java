package ru.practicum.stats.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.server.config.ConfigUtil;
import ru.practicum.stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(ConfigUtil.DATE);

    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "stringToLocalDateTime")
    EndpointHit toEntity(EndpointHitDto endpointHitDto);

    @Named("stringToLocalDateTime")
    default LocalDateTime stringToLocalDateTime(String timestamp) {
        return timestamp != null ? LocalDateTime.parse(timestamp, FORMATTER) : null;
    }
}