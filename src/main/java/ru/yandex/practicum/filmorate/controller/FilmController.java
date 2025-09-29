package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) {
        Film validFilm = FilmValidator.filmValid(film)
                .toBuilder()
                .id(setId())
                .build();
        films.put(validFilm.getId(), validFilm);
        log.info("Добавлен новый фильм: {}", validFilm);
        return validFilm;
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody @NotNull Film film) {
        if (film.getId() <= 0) {
            log.warn("Id {} должен быть больше 0", film.getId());
            throw new IllegalArgumentException("Id должен быть больше 0");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new IllegalArgumentException("Фильм с id " + film.getId() + " не найден");
        }
        Film validFilm = FilmValidator.filmValid(film)
                .toBuilder()
                .id(film.getId())
                .build();
        films.put(validFilm.getId(), validFilm);
        log.info("Данные фильма с id {} обновлены: Новые данные: {}", validFilm.getId(), validFilm);
        return validFilm;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    private Long setId() {
        return films.keySet().stream()
                .max(Long::compareTo)
                .map(id -> id + 1)
                .orElse(1L);
    }
}