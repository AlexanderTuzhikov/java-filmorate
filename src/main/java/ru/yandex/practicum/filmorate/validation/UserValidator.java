package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class UserValidator {
    public static User userValid(@NotNull User user) {
        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(loginValid(user))
                .name(nameValid(user))
                .birthday(user.getBirthday())
                .build();
    }

    private static String loginValid(@NotNull User user) {
        if (user.getLogin().contains(" ")) {
            log.warn("Передан некорректный login: {}", user);
            throw new ValidationException("Логин не может содержать пробелы;");
        }

        return user.getLogin();
    }

    private static String nameValid(@NotNull User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Передано пустое значение name: {}, вместо него будет использован login", user);
            return user.getLogin();
        }

        return user.getName();
    }
}

