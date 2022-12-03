package ru.practicum.ewmmain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.practicum.ewmmain.dto.user.NewUserRequest;
import ru.practicum.ewmmain.dto.user.UserDto;
import ru.practicum.ewmmain.dto.user.UserMapper;
import ru.practicum.ewmmain.model.User;
import ru.practicum.ewmmain.storage.UserRepository;
import ru.practicum.ewmmain.utils.CustomPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(NewUserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("User with email {} already exists", userRequest.getEmail());

        }
        User user = userRepository.save(userMapper.toUser(userRequest));
        log.info("User with id = {} has been created", user.getId());
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll(List<Long> ids, int from, int size) {
        List<User> users;
        Pageable page = CustomPageRequest.of(from, size);

        if (CollectionUtils.isEmpty(ids)) {
            users = userRepository.findAll(page).getContent();
        } else {
            users = userRepository.findByIdIn(ids, page);
        }

        log.info("For conditions ids = {}, from = {}, size = {}: {} users has been founded",
                ids, from, size, users.size());
        return userMapper.userDtos(users);
    }

    @Override
    public void delete(long userId) {
        userRepository.deleteById(userId);
        log.info("User with id = {} has been deleted", userId);
    }
}
