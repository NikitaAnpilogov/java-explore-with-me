package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto addNewUser(NewUserRequest newUserRequest) {
        log.info("Добавление нового пользователя: {}", newUserRequest);
        User user = userRepository.save(UserMapper.map(newUserRequest));
        log.info("Пользователь добавлен: {}", user);
        return UserMapper.map(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не существует.", "Пользователь с id= " + userId + " не найден");
        }
        userRepository.deleteById(userId);
        log.info("Пользователь c id {} удален.", userId);
    }

    @Transactional
    @Override
    public List<UserDto> getListUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение списка пользователей: {}, from={}, size={}", ids, from, size);
        PageRequest page = PageRequest.of(from / size, size);
        return (ids != null) ? userRepository.findByIdIn(ids, page)
                .stream()
                .map(UserMapper::map)
                .collect(Collectors.toList())
                : userRepository.findAll(page).stream()
                .map(UserMapper::map)
                .collect(Collectors.toList());
    }
}