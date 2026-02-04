package ru.practicum.main.mappers;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.models.Location;

@Component
public class LocationMapper {

    public LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        LocationDto dto = new LocationDto();
        dto.setLat(location.getLat());
        dto.setLon(location.getLon());
        return dto;
    }
}