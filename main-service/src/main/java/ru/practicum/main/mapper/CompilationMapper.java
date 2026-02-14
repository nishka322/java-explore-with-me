package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.model.Compilation;
import java.util.List;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    CompilationDto mapToCompilationDto(Compilation compilation);

    List<CompilationDto> mapToListCompilationDto(List<Compilation> compilations);
}