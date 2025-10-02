package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User postUser(User user) {
        User validUser = UserValidator.userValid(user);
        return userStorage.createUser(validUser);
    }

    public User putUser(@NotNull User user) {
        if (user.getId() == null) {
            log.warn("Ошибка валидации: id=null");
            throw new ValidationException("Ошибка валидации: id=null");
        }

        User validUser = UserValidator.userValid(user);
        return userStorage.updateUser(validUser);
    }

    public List<User> getUsers() {
        return userStorage.getAllUsers();
    }

    public User getUser(Long id) {
        return userStorage.getUser(id);
    }

    public void putFriend(Long id, Long friendId) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(friendId);
        user.addFriend(friend);
        friend.addFriend(user);
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(friendId);
        user.removeFriend(friend);
        friend.removeFriend(user);
    }

    public List<User> getFriends(Long id) {
        User user = userStorage.getUser(id);

        if (user.getFriends().isEmpty()) {
            return List.of();
        }

        return user.getFriends().stream()
                .map(userStorage::getUser)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = userStorage.getUser(id);
        User otherUser = userStorage.getUser(otherId);

        if (user.getFriends().isEmpty()) {
            return List.of();
        }

        Set<Long> userFriends = user.getFriends();
        userFriends.retainAll(otherUser.getFriends());

        if (userFriends.isEmpty()) {
            return List.of();
        }

        return userFriends.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toList());
    }

}
