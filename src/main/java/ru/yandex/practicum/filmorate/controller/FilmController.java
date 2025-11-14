package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@AllArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FilmDto> postFilm(@Valid @RequestBody NewFilmRequest request) {
        log.info("Получен запрос на добавление фильма {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(filmService.postFilm(request));
    }

    @PutMapping
    public ResponseEntity<FilmDto> putFilm(@Valid @RequestBody @NotNull UpdateFilmRequest request) {
        log.info("Получен запрос на обновление фильма id={}", request.getId());
        return ResponseEntity.ok().body(filmService.putFilm(request));
    }

    @GetMapping
    public ResponseEntity<List<FilmDto>> getFilms() {
        log.info("Получен запрос на получение списка фильмов");
        System.out.println(filmService.getFilms().stream().map(FilmDto::getId).toList());
        return ResponseEntity.ok().body(filmService.getFilms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilm(@PathVariable("id") Long id) {
        log.info("Получен запрос на получение фильма");
        return ResponseEntity.ok().body(filmService.getFilm(id));
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Void> deleteFilm(@PathVariable("filmId") Long filmId) {
        log.info("Получен запрос на удаление фильма  id={}", filmId);
        filmService.deleteFilm(filmId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> putLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос на добавление лайка к фильму id={}, от пользователя id={}", id, userId);
        filmService.putLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.deleteLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDto>> getFilmsPopular(@RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        log.info("Получен запрос на получение списка из count={} популярных фильмов", count);
        return ResponseEntity.ok().body(filmService.getFilmsPopular(count));
    }
}