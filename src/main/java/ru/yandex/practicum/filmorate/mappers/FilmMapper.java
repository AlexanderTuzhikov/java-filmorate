package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {
    public static Film mapToFilm(@NotNull NewFilmRequest request) {
        return Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .mpaName(request.getMpaName())
                .build();
    }

    public static FilmDto mapToFilmDto(@NotNull Film film) {
        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpaName(film.getMpaName())
                .build();
    }

    @Contract("_, _ -> param1")
    public static Film updateFilmFields(Film film, @NotNull UpdateFilmRequest request) {
        if (request.hasName()) {
            film.toBuilder()
                    .name(request.getName())
                    .build();
        }

        if (request.hasDescription()) {
            film.toBuilder()
                    .description(request.getDescription())
                    .build();
        }

        if (request.hasReleaseDate()) {
            film.toBuilder()
                    .releaseDate(request.getReleaseDate())
                    .build();
        }

        if (request.hasDuration()) {
            film.toBuilder()
                    .duration(request.getDuration())
                    .build();
        }

        if (request.hasMpa()) {
            film.toBuilder()
                    .mpaName(request.getMpaName())
                    .build();
        }

        return film;
    }
}