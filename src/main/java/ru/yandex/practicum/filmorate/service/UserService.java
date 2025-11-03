package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.friendship.FriendshipDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundUser;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.mappers.UserMapper.*;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserDbRepository userRepository;
    private final FriendshipDbRepository friendshipRepository;

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
            throw new NotFoundUser("Пользователь для обновления не найден");
        }

        User user = updateUserFields(findUser.get(), request);
        User validUser = UserValidator.userValid(user);
        User updatedUser = userRepository.update(validUser);

        return mapToUserDto(updatedUser);
    }

    public UserDto getUser(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundUser("Пользователь с id=" + id + " не найден"));
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public void putFriend(Long userId, Long friendId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<User> friend = userRepository.findById(friendId);

        if (user.isEmpty() || friend.isEmpty()) {
            log.warn("Попытка создать дружбу не существующих пользователей userId= {}, friendId= {}", userId, friendId);
            throw new NotFoundUser("Пользователи для добавления в дружбу не найдены");
        }

        boolean status = friendshipRepository.save(userId, friendId);

        if (!status) {
            log.error("Ошибка сервера при обработки запроса добавления в дружбу пользователей userId= {}," +
                    " friendId= {}", userId, friendId);
            throw new InternalServerException("Ошибка сервера при добавлении в друзья");
        }

        log.info("Пользователь id= {} добавил друга id= {} статус дружбы=CONFIRMED. " +
                "Пользователь id= {} получил запрос на добавление в друзья от id= {} статус дружбы=NOT_CONFIRMED.", userId, friendId, friendId, userId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<User> friend = userRepository.findById(friendId);

        if (user.isEmpty() || friend.isEmpty()) {
            log.warn("Попытка удалить дружбу не существующих пользователей userId= {}, friendId= {}", userId, friendId);
            throw new NotFoundUser("Пользователи для удаления дружбы не найдены");
        }

        friendshipRepository.delete(userId, friendId);
    }

    public List<UserDto> getFriends(Long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            log.warn("Попытка получить друзей не существующего пользователя userId= {}", userId);
            throw new NotFoundUser("Пользователь не найден");
        }

        return friendshipRepository.findAllFriends(userId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(Long userId, Long friendId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<User> friend = userRepository.findById(friendId);

        if (user.isEmpty() || friend.isEmpty()) {
            log.warn("Попытка запроса общих друзей для не существующих пользователей userId= {}, friendId= {}", userId, friendId);
            throw new NotFoundUser("Пользователи для запроса общих друзей не найдены");
        }

        return friendshipRepository.findCommonFriends(userId, friendId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }
}
