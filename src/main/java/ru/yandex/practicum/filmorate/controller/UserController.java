package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> postUser(@Valid @RequestBody NewUserRequest request) {
        log.info("Получен запрос на добавление пользователя {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.postUser(request));
    }

    @PutMapping
    public ResponseEntity<User> putUser(@Valid @RequestBody UpdateUserRequest request) {
        log.info("Получен запрос на обновление пользователя id={}", request.getId());
        return ResponseEntity.ok().body(userService.putUser(request));
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        log.info("Получен запрос на получение списка пользователей");
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        log.info("Получен запрос на получение пользователя");
        return ResponseEntity.ok().body(userService.getUser(id));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> putFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        log.info("Получен запрос на добавление в друзья id={} от пользователя id={}", id, friendId);
        userService.putFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        log.info("Получен запрос на удаление из друзей id={} от пользователя id={}", id, friendId);
        userService.deleteFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable("id") Long id) {
        log.info("Получен запрос на получение списка друзей");
        return ResponseEntity.ok().body(userService.getFriends(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable("id") Long id, @PathVariable("otherId") Long otherId) {
        log.info("Получен запрос на получение списка общих друзей id={} и id={}", id, otherId);
        return ResponseEntity.ok().body(userService.getCommonFriends(id, otherId));
    }

}