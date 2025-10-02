package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
@AllArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Film> postFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма {}", film);
        return ResponseEntity.status(HttpStatus.CREATED).body(filmService.postFilm(film));
    }

    @PutMapping
    public ResponseEntity<Film> putFilm(@Valid @RequestBody @NotNull Film film) {
        log.info("Получен запрос на обновление фильма id={}", film.getId());
        return ResponseEntity.ok().body(filmService.postFilm(film));
    }

    @GetMapping
    public ResponseEntity<List<Film>> getFilms() {
        log.info("Получен запрос на получение списка фильмов");
        return ResponseEntity.ok().body(filmService.getFilms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilm(@PathVariable("id") Long id) {
        log.info("Получен запрос на получение фильма");
        return ResponseEntity.ok().body(filmService.getFilm(id));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> putLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос на добавление лайка к фильму id={}, от пользователя id={}", id, userId);
        filmService.putLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос на удаление лайка к фильму id={}, от пользователя id={}", id, userId);
        filmService.deleteLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getFilmsPopular(@RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        log.info("Получен запрос на получение списка из count={} популярных фильмов", count);
        return ResponseEntity.ok().body(filmService.getFilmsPopular(count));
    }

}