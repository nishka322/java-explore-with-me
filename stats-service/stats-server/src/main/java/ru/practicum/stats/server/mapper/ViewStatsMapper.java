package ru.practicum.stats.server.mapper;

import org.mapstruct.Mapper;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.ViewStats;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ViewStatsMapper {

    ViewStatsDto toDto(ViewStats viewStats);

    List<ViewStatsDto> toDtoList(List<ViewStats> viewStats);
}