package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.event.EventDbRepository;
import ru.yandex.practicum.filmorate.dal.db.film.FilmDbRepository;
import ru.yandex.practicum.filmorate.dal.db.like.LikeDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dto.event.NewEventRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundFilm;
import ru.yandex.practicum.filmorate.exception.NotFoundUser;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

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
    private final LikeDbRepository likeRepository;
    private final EventDbRepository eventRepository;

    public FilmDto postFilm(NewFilmRequest request) {
        Film film = mapToFilm(request);
        Film validFilm = filmValid(film);
        Film savedFilm = filmRepository.save(validFilm);
        return mapToFilmDto(savedFilm);
    }

    public FilmDto putFilm(@NotNull UpdateFilmRequest request) {
        Optional<Film> findFilm = filmRepository.findById(request.getId());

        if (findFilm.isEmpty()) {
            log.warn("Фильм userId= {} для обновления не найден", request.getId());
            throw new NotFoundException("Фильм для обновления не найден");
        }

        Film film = updateFilmFields(findFilm.get(), request);
        Film validFilm = filmValid(film);
        Film updatedFilm = filmRepository.update(validFilm);
        return mapToFilmDto(updatedFilm);
    }

    public List<FilmDto> getFilms() {
        return filmRepository.findAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto getFilm(Long filmId) {
        return filmRepository.findById(filmId)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
    }

    public void deleteFilm(Long filmId) {
        filmRepository.delete(filmId);
    }

    public void putLike(Long filmId, Long userId) {
        filmRepository.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден: id=" + filmId));
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден: id=" + userId));
        likeRepository.save(filmId, userId);
        saveEvent(userId, filmId, Operation.ADD);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmRepository.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден: id=" + filmId));
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден: id=" + userId));
        likeRepository.delete(filmId, userId);
        saveEvent(userId, filmId, Operation.REMOVE);
    }

    public Collection<FilmDto> getSortedFilms(Long directorId, String sortBy) {
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new InternalServerException("Некорректный параметр сортировки.");
        }
        return filmRepository.getSortedFilms(directorId, sortBy);
    }

    public List<FilmDto> getCommonFilms(long userId, long friendId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: id=" + userId));
        userRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: id=" + friendId));

        List<Film> films = filmRepository.findCommonFilms(userId, friendId);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDto> getFilmsPopular(int count) {
        return likeRepository.findPopularFilms()
                .stream()
                .map(filmRepository::findById)
                .flatMap(Optional::stream)
                .map(FilmMapper::mapToFilmDto)
                .limit(count)
                .toList();
    }

    public List<FilmDto> getFilmsPopularByGenreId(int count, long genreId) {
        log.debug("Получение {} популярных фильмов по жанру {}", count, genreId);
        List<Long> popularFilms = likeRepository.findPopularFilms();
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
        log.debug("Получение {} популярных фильмов за {} год", count, year);
        List<Long> popularFilms = likeRepository.findPopularFilms();
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
        log.debug("Получение {} популярных фильмов по жанру {} за {} год", count, genreId, year);
        List<Long> popularFilms = likeRepository.findPopularFilms();
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

    private void saveEvent(Long userId, Long entityId, Operation operation) {
        NewEventRequest newEvent = NewEventRequest.builder()
                .userId(userId)
                .entityId(entityId)
                .eventType(EventType.LIKE)
                .operation(operation)
                .build();

        eventRepository.save(newEvent);
    }
}
