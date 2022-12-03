package ru.practicum.ewmmain.dto.user;

import org.springframework.stereotype.Component;
import ru.practicum.ewmmain.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toUser(NewUserRequest dto) {
        return new User(dto.getName(), dto.getEmail());
    }

    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }

    public List<UserDto> userDtos(List<User> users) {
        return users.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }
}
