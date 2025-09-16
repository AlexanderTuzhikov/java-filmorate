package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    private User postUser(@RequestBody User user) {
        UserValidator.userValidator(user);
        User newUser = User.builder()
                .id(setId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(UserValidator.resolveName(user))
                .birthday(user.getBirthday())
                .build();

        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь: {}", newUser);
        return newUser;
    }

    @PutMapping
    private User putUser(@RequestBody User user) {
        UserValidator.userValidator(user);
        User oldUser = users.get(user.getId());
        User newUser = oldUser.toBuilder()
                .email(user.getEmail())
                .login(user.getLogin())
                .name(UserValidator.resolveName(user))
                .birthday(user.getBirthday())
                .build();

        users.put(newUser.getId(), newUser);
        log.info("Данные пользователя с id {} обновлены", newUser.getId());
        return newUser;
    }

    @GetMapping
    private List<User> getFilms() {
        return new ArrayList<>(users.values());
    }

    private Long setId() {
        return users.keySet().stream()
                .max(Long::compareTo)
                .map(id -> id + 1)
                .orElse(1L);
    }
}
