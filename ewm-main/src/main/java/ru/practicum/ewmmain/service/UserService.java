package ru.practicum.ewmmain.service;

import ru.practicum.ewmmain.dto.user.NewUserRequest;
import ru.practicum.ewmmain.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest userRequest);

    List<UserDto> findAll(List<Long> ids, int from, int size);

    void delete(long userId);

}
