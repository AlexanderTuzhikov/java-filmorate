package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilm(Long id);

    List<Film> getAllFilms();

    void deleteFilm(Long id);

    void deleteAllFilms();
}
