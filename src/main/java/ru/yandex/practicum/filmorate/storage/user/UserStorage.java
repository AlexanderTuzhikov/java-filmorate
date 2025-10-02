package ru.yandex.practicum.filmorate.storage.user;

import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    User getUser(Long id);

    List<User> getAllUsers();

    void deleteUser(Long id);

    void deleteAllUsers();
}
