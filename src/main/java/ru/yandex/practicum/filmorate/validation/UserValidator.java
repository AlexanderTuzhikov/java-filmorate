package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class UserValidator {
    public static User userValid(@NotNull NewUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .login(loginValid(request))
                .name(nameValid(request))
                .birthday(request.getBirthday())
                .build();
    }

    private static String loginValid(@NotNull NewUserRequest request) {
        if (request.getLogin().contains(" ")) {
            log.warn("Передан некорректный login: {}", request);
            throw new ValidationException("Логин не может содержать пробелы;");
        }

        return request.getLogin();
    }

    private static String nameValid(@NotNull NewUserRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            log.warn("Передано пустое значение name: {}, вместо него будет использован login", request);
            return request.getLogin();
        }

        return request.getName();
    }
}

