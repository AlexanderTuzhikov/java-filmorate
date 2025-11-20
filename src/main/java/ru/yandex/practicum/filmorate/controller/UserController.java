package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> postUser(@Valid @RequestBody NewUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.postUser(request));
    }

    @PutMapping
    public ResponseEntity<UserDto> putUser(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok().body(userService.putUser(request));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok().body(userService.getUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteFilm(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> putFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        userService.putFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        userService.deleteFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<UserDto>> getFriends(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok().body(userService.getFriends(userId));
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public ResponseEntity<List<UserDto>> getCommonFriends(@PathVariable("userId") Long userId, @PathVariable("otherId") Long otherId) {
        return ResponseEntity.ok().body(userService.getCommonFriends(userId, otherId));
    }

    @GetMapping("/{userId}/feed")
    public ResponseEntity<List<EventDto>> getUserFeed(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok().body(userService.getUserFeed(userId));
    }

    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<FilmDto>> getRecommendations(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok().body(userService.getRecommendations(userId));
    }
}