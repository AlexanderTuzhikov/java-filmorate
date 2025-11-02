package ru.yandex.practicum.filmorate.dal.memory;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.NotFoundFilm;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
@Deprecated
public class InMemoryFilmInMemoryRepository implements BaseInMemoryRepository<Film> {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(@NotNull Film film) {
        film = film.toBuilder()
                .id(setId())
                .build();
        log.info("Создан новый фильм: {}", film);
        saveFilm(film);

        return films.get(film.getId());
    }

    @Override
    public Film update(@NotNull Film film) {
        if (films.containsKey(film.getId())) {
            Film existing = get(film.getId());
            film = mergeFilmData(existing, film);
            log.info("Обновлен фильм: {}", film);
            saveFilm(film);
        } else {
            log.warn("Попытка обновить не существующий фильм с id={}", film.getId());
            throw new NotFoundFilm("Фильм с id=" + film.getId() + " не найден");
        }

        return films.get(film.getId());
    }

    @Override
    public Film get(Long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            log.warn("Попытка получить не существующий фильм с id={}", id);
            throw new NotFoundFilm("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public List<Film> getAll() {
        if (films.isEmpty()) {
            log.warn("Запрошен пустой список фильмов");
            return Collections.emptyList();
        }

        return new ArrayList<>(films.values());
    }

    @Override
    public void delete(Long id) {
        if (!films.containsKey(id)) {
            log.info("Удален фильм: {}", films.get(id));
            films.remove(id);
        } else {
            log.warn("Попытка удалить не существующий фильм с id={}", id);
            throw new NotFoundFilm("Фильм с id=" + id + " не найден");
        }
    }

    private Long setId() {
        return films.keySet().stream()
                .max(Long::compareTo)
                .map(id -> id + 1)
                .orElse(1L);
    }

    private void saveFilm(@NotNull Film film) {
        films.put(film.getId(), film);
        log.info("Сохранен фильм: {}", film);
    }

    private Film mergeFilmData(@NotNull Film existing, @NotNull Film newData) {
        return newData.toBuilder()
                .id(newData.getId())
                .name(newData.getName())
                .description(newData.getDescription())
                .releaseDate(newData.getReleaseDate())
                .duration(newData.getDuration())
                .likes(existing.getLikes())
                .build();
    }
}
