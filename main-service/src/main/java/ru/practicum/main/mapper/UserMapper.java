package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.dto.user.UserShortDto;
import ru.practicum.main.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUserModel(UserDto userDto);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    List<UserDto> toUserDtoList(List<User> usersList);
}