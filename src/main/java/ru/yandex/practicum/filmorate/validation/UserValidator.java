package ru.yandex.practicum.filmorate.validation;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserValidator {
    private static final Logger log = LoggerFactory.getLogger(UserValidator.class);

    public static void userValidator(@NotNull User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Передан некорректный email: {}", user);
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @;");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().equals(" ")) {
            log.warn("Передан некорректный login: {}", user);
            throw new ValidationException("Логин не может быть пустым и содержать пробелы;");
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем; Передана дата рождения {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем;");

        }
    }

    public static String resolveName(@NotNull User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Передано пустое значение name: {}, вместо него будет использован login", user);
            return user.getLogin();
        }

        return user.getName();
    }
}

