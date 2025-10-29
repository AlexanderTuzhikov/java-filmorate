package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.memory.InMemoryFilmInMemoryRepository;
import ru.yandex.practicum.filmorate.dal.memory.InMemoryUserInMemoryRepository;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private final InMemoryFilmInMemoryRepository filmStorage;
    private final InMemoryUserInMemoryRepository userStorage;

    public Film postFilm(Film film) {
        Film validFilm = FilmValidator.filmValid(film);
        return filmStorage.create(validFilm);
    }

    public Film putFilm(@NotNull Film film) {
        if (film.getId() == null) {
            log.warn("Ошибка валидации: id=null");
            throw new ValidationException("Ошибка валидации: id=null");
        }

        Film validFilm = FilmValidator.filmValid(film);
        return filmStorage.update(validFilm);
    }

    public List<Film> getFilms() {
        return filmStorage.getAll();
    }

    public Film getFilm(Long id) {
        return filmStorage.get(id);
    }

    public void putLike(Long id, Long userId) {
        Film film = filmStorage.get(id);
        User user = userStorage.get(userId);
        film.addLike(user);
    }

    public void deleteLike(Long id, Long userId) {
        Film film = filmStorage.get(id);
        User user = userStorage.get(userId);
        film.removeLike(user);
    }

    public List<Film> getFilmsPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

}
