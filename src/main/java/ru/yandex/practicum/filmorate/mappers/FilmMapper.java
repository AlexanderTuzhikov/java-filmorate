package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class FilmMapper {

    public static Film mapToFilm(@NotNull NewFilmRequest request) {

        return Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .mpa(request.getMpa())
                .genres(request.getGenres())
                .directors(request.getDirectors())
                .build();
    }

    public static FilmDto mapToFilmDto(@NotNull Film film) {
        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa())
                .genres(film.getGenres().stream()
                        .sorted(Comparator.comparing(Genre::getId))
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .directors(film.getDirectors().stream()
                        .sorted(Comparator.comparing(Director::getId))
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build();
    }

    public static Film updateFilmFields(@NotNull Film film, @NotNull UpdateFilmRequest request) {
        Film.FilmBuilder builder = film.toBuilder();

        if (request.hasName()) {
            builder.name(request.getName());
        }

        if (request.hasDescription()) {
            builder.description(request.getDescription());
        }

        if (request.hasReleaseDate()) {
            builder.releaseDate(request.getReleaseDate());

        }

        if (request.hasDuration()) {
            builder.duration(request.getDuration());
        }

        if (request.hasReleaseDate()) {
            builder.releaseDate(request.getReleaseDate());

        }

        if (request.hasMpa()) {
            builder.mpa(request.getMpa());
        }

        if (!request.hasGenres()) {
            builder.genres(request.getGenres());
        }

        if (!request.hasDirectors()) {
            builder.directors(request.getDirectors());
        }

        return builder.build();
    }
}