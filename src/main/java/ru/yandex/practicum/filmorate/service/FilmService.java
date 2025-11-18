package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.film.FilmDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.mappers.FilmMapper.*;
import static ru.yandex.practicum.filmorate.validation.FilmValidator.filmValid;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private final FilmDbRepository filmRepository;
    private final UserDbRepository userRepository;
    private final LikeService likeService;

    public FilmDto postFilm(NewFilmRequest request) {
        Film film = mapToFilm(request);
        Film validFilm = filmValid(film);
        Film savedFilm = filmRepository.save(validFilm);
        return mapToFilmDto(savedFilm);
    }

    public FilmDto putFilm(@NotNull UpdateFilmRequest request) {
        Film film = checkFilmExists(request.getId());
        Film updatedFilmLine = updateFilmFields(film, request);
        Film validFilm = filmValid(updatedFilmLine);
        Film updatedFilm = filmRepository.update(validFilm);
        return mapToFilmDto(updatedFilm);
    }

    public List<FilmDto> getFilms() {
        return filmRepository.findAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto getFilm(Long filmId) {
        Film film = checkFilmExists(filmId);
        return mapToFilmDto(film);
    }

    public void deleteFilm(Long filmId) {
        filmRepository.delete(filmId);
    }

    public void putLike(Long filmId, Long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);
        likeService.postLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);
        likeService.deleteLike(filmId, userId);
    }

    public Collection<FilmDto> getSortedFilms(Long directorId, String sortBy) {
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new InternalServerException("Некорректный параметр сортировки.");
        }

        return filmRepository.getSortedFilms(directorId, sortBy);
    }

    public List<FilmDto> getCommonFilms(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);

        List<Film> films = filmRepository.findCommonFilms(userId, friendId);

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDto> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Параметр query не может быть пустым");
        }
        if (by == null || by.isBlank()) {
            throw new ValidationException("Параметр by не может быть пустым");
        }

        List<String> fields = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        boolean byTitle = fields.contains("title");
        boolean byDirector = fields.contains("director");

        if (!byTitle && !byDirector) {
            throw new ValidationException("Параметр by должен содержать 'title', 'director' или оба значения через запятую");
        }

        List<Film> films = filmRepository.searchFilms(query, byTitle, byDirector);

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDto> getFilmsPopular(int count) {
        return likeService.getFilmsPopular()
                .stream()
                .map(filmRepository::findById)
                .flatMap(Optional::stream)
                .map(FilmMapper::mapToFilmDto)
                .limit(count)
                .toList();
    }

    public List<FilmDto> getFilmsPopularByGenreId(int count, long genreId) {
        List<Long> popularFilms = likeService.getFilmsPopular();
        List<Long> filmsByGenre = filmRepository.findAllFilmsByGenreId(genreId);

        return popularFilms.stream()
                .filter(filmsByGenre::contains)
                .limit(count)
                .map(filmRepository::findById)
                .flatMap(Optional::stream)
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDto> getFilmsPopularByYear(int count, long year) {
        List<Long> popularFilms = likeService.getFilmsPopular();
        List<Long> filmsByYear = filmRepository.findFilmsByYear(year);

        return popularFilms.stream()
                .filter(filmsByYear::contains)
                .limit(count)
                .map(filmRepository::findById)
                .flatMap(Optional::stream)
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDto> getFilmsPopularByGenreIdAndYear(int count, long genreId, long year) {
        List<Long> popularFilms = likeService.getFilmsPopular();
        List<Long> filmsByYear = filmRepository.findFilmsByYear(year);
        List<Long> filmsByGenre = filmRepository.findAllFilmsByGenreId(genreId);

        return popularFilms.stream()
                .filter(filmsByYear::contains)
                .filter(filmsByGenre::contains)
                .limit(count)
                .map(filmRepository::findById)
                .flatMap(Optional::stream)
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDto> getRecommendations(Long userId) {
        return filmRepository.findRecommendationsFilms(userId).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    private void checkUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Film checkFilmExists(Long filmId) {
        return filmRepository.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
    }
}
