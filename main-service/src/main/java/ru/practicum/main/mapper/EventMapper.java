package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.main.config.ConfigUtil;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.dto.event.NewEventDto;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class})
public interface EventMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(ConfigUtil.DATE);

    @Mapping(target = "participantLimit", expression = "java((long) event.getParticipantLimit())")
    @Mapping(target = "createdOn", expression = "java(event.getCreatedOn() != null ? event.getCreatedOn().format(FORMATTER) : null)")
    @Mapping(target = "views", constant = "0L")
    @Mapping(target = "confirmedRequests", constant = "0L")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "category", source = "category", qualifiedByName = "mapIdToCategory")
    @Mapping(target = "requestModeration", expression = "java(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)")
    Event toEventModel(NewEventDto dto);

    @Mapping(target = "views", constant = "0L")
    @Mapping(target = "confirmedRequests", constant = "0L")
    EventShortDto toEventShortDto(Event event);

    List<EventShortDto> toEventShortDtoList(List<Event> events);

    List<EventFullDto> toEventFullDtoList(List<Event> events);

    @Named("mapIdToCategory")
    default Category mapIdToCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }
}