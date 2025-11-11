package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
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
    public ResponseEntity<UserDto> putUser(@Valid @RequestBody UpdateUserRequest request) {
        log.info("Получен запрос на обновление пользователя id={}", request.getId());
        return ResponseEntity.ok().body(userService.putUser(request));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers() {
        log.info("Получен запрос на получение списка пользователей");
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable("userId") Long userId) {
        log.info("Получен запрос на получение пользователя");
        return ResponseEntity.ok().body(userService.getUser(userId));
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> putFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("Получен запрос на добавление в друзья id={} от пользователя id={}", userId, friendId);
        userService.putFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("Получен запрос на удаление из друзей id={} от пользователя id={}", userId, friendId);
        userService.deleteFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<UserDto>> getFriends(@PathVariable("userId") Long userId) {
        log.info("Получен запрос на получение списка друзей");
        return ResponseEntity.ok().body(userService.getFriends(userId));
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public ResponseEntity<List<UserDto>> getCommonFriends(@PathVariable("userId") Long userId, @PathVariable("otherId") Long otherId) {
        log.info("Получен запрос на получение списка общих друзей id={} и id={}", userId, otherId);
        return ResponseEntity.ok().body(userService.getCommonFriends(userId, otherId));
    }

    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<FilmDto>> getRecommendations(@PathVariable("userId") Long userId) {
        log.info("Получен запрос на получение рекомендованных фильмов от id={} ", userId);
        return ResponseEntity.ok().body(userService.getRecommendations(userId));
    }
}