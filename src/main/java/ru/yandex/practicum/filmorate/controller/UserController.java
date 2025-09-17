package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User postUser(@Valid @RequestBody User user) {
        User validUser = UserValidator.userValid(user)
                .toBuilder()
                .id(setId())
                .build();
        users.put(validUser.getId(), validUser);
        log.info("Добавлен новый пользователь: {}", validUser);
        return validUser;
    }

    @PutMapping
    public User putUser(@Valid @RequestBody @NotNull User user) {
        if (user.getId() <= 0) {
            log.warn("Id {} должен быть больше 0", user.getId());
            throw new IllegalArgumentException("Id должен быть больше 0");
        }

        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new IllegalArgumentException("Пользователь с id " + user.getId() + " не найден");
        }

        User validUser = UserValidator.userValid(user)
                .toBuilder()
                .id(user.getId())
                .build();

        users.put(user.getId(), validUser);
        log.info("Данные пользователя с id {} обновлены. Новые данные: {}", validUser.getId(), validUser);
        return validUser;
    }

    @GetMapping
    public List<User> getFilms() {
        return new ArrayList<>(users.values());
    }

    private Long setId() {
        return users.keySet().stream()
                .max(Long::compareTo)
                .map(id -> id + 1)
                .orElse(1L);
    }
}