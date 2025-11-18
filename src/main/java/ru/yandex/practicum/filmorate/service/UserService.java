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
import java.util.Optional;

import static ru.yandex.practicum.filmorate.mappers.UserMapper.*;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserDbRepository userRepository;
    private final FriendshipService friendshipService;
    private final FilmService filmService;
    private final FeedService feedService;

    public UserDto postUser(NewUserRequest request) {
        User user = mapToUser(request);
        User validUser = UserValidator.userValid(user);
        User savedUser = userRepository.save(validUser);
        return mapToUserDto(savedUser);
    }

    public UserDto putUser(@NotNull UpdateUserRequest request) {
        Optional<User> findUser = userRepository.findById(request.getId());

        if (findUser.isEmpty()) {
            log.warn("Пользователь userId= {} для обновления не найден", request.getId());
            throw new NotFoundException("Пользователь для обновления не найден");
        }

        User user = updateUserFields(findUser.get(), request);
        User validUser = UserValidator.userValid(user);
        User updatedUser = userRepository.update(validUser);

        return mapToUserDto(updatedUser);
    }

    public UserDto getUser(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public void putFriend(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        friendshipService.postFriendship(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        friendshipService.deleteFriendship(userId, friendId);
    }

    public void deleteUser(Long userId) {
        userRepository.delete(userId);
    }

    public List<UserDto> getFriends(Long userId) {
        checkUserExists(userId);

        return friendshipService.getFriends(userId);
    }

    public List<UserDto> getCommonFriends(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);

        return friendshipService.getCommonFriends(userId, friendId);
    }

    public List<EventDto> getUserFeed(Long userId) {
        checkUserExists(userId);

        return feedService.getUserFeed(userId).stream()
                .map(EventMapper::mapEventDto)
                .sorted(Comparator.comparing(EventDto::getTimestamp))
                .toList();
    }

    public List<FilmDto> getRecommendations(Long userId) {
        return filmService.getRecommendations(userId);
    }

    private void checkUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}
