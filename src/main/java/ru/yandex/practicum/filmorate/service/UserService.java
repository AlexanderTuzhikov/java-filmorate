package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.EventMapper;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.Comparator;
import java.util.List;

import static ru.yandex.practicum.filmorate.mappers.UserMapper.*;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserDbRepository userRepository;
    private final FriendshipService friendshipService;
    private final FilmService filmService;
    private final FeedService feedService;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    public UserDto postUser(NewUserRequest request) {
        log.info("Получен запрос на добавление пользователя {}", request);
        User user = userMapper.mapToUser(request);
        User validUser = UserValidator.userValid(user);
        User savedUser = userRepository.save(validUser);
        return userMapper.mapToUserDto(savedUser);
    }

    public UserDto putUser(@NotNull UpdateUserRequest request) {
        log.info("Получен запрос на обновление пользователя id={}", request.getId());
        User findUser = checkUserExists(request.getId());
        User user = updateUserFields(findUser, request);
        User validUser = UserValidator.userValid(user);
        User updatedUser = userRepository.update(validUser);

        return userMapper.mapToUserDto(updatedUser);
    }

    public UserDto getUser(Long userId) {
        log.info("Получен запрос на получение пользователя");
        User user = checkUserExists(userId);
        return userMapper.mapToUserDto(user);
    }

    public List<UserDto> getUsers() {
        log.info("Получен запрос на получение списка пользователей");
        return userRepository.findAll().stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    public void putFriend(Long userId, Long friendId) {
        log.info("Получен запрос на добавление в друзья id={} от пользователя id={}", userId, friendId);
        checkUserExists(userId);
        checkUserExists(friendId);
        friendshipService.postFriendship(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Получен запрос на удаление из друзей id={} от пользователя id={}", userId, friendId);
        checkUserExists(userId);
        checkUserExists(friendId);
        friendshipService.deleteFriendship(userId, friendId);
    }

    public void deleteUser(Long userId) {
        log.info("Получен запрос на удаление пользователя  id={}", userId);
        userRepository.delete(userId);
    }

    public List<UserDto> getFriends(Long userId) {
        log.info("Получен запрос на получение списка друзей");
        checkUserExists(userId);

        return friendshipService.getFriends(userId);
    }

    public List<UserDto> getCommonFriends(Long userId, Long friendId) {
        log.info("Получен запрос на получение списка общих друзей id={} и id={}", userId, friendId);
        checkUserExists(userId);
        checkUserExists(friendId);

        return friendshipService.getCommonFriends(userId, friendId);
    }

    public List<EventDto> getUserFeed(Long userId) {
        log.info("Получен запрос на получение ленты событий пользователя id={}", userId);
        checkUserExists(userId);

        return feedService.getUserFeed(userId).stream()
                .map(eventMapper::mapEventDto)
                .sorted(Comparator.comparing(EventDto::getTimestamp))
                .toList();
    }

    public List<FilmDto> getRecommendations(Long userId) {
        log.info("Получен запрос на получение рекомендованных фильмов от id={} ", userId);
        return filmService.getRecommendations(userId);
    }

    private User checkUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}
