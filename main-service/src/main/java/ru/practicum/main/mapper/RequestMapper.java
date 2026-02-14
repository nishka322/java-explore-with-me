package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.dto.request.RequestDto;
import ru.practicum.main.model.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? request.getStatus().toString() : null)")
    RequestDto toRequestDto(Request request);

    List<RequestDto> toRequestDtoList(List<Request> requests);
}