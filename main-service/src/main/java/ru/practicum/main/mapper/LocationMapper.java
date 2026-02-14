package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toLocationDto(Location location);
}