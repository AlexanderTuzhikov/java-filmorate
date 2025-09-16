package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    private Film postFilm(@RequestBody Film film) {
        FilmValidator.filmValidator(film);
        Film newFilm = Film.builder()
                .id(setId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .build();
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен новый фильм: {}", newFilm);
        return newFilm;
    }

    @PutMapping
    private Film putFilm(@RequestBody Film film) {
        FilmValidator.filmValidator(film);
        Film oldFilm = films.get(film.getId());

        Film newFilm = oldFilm.toBuilder()
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .build();
        films.put(newFilm.getId(), newFilm);
        log.info("Данные фильма с id {} обновлены", newFilm.getId());
        return newFilm;
    }

    @GetMapping
    private List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    private Long setId() {
        return films.keySet().stream()
                .max(Long::compareTo)
                .map(id -> id + 1)
                .orElse(1L);
    }
}
