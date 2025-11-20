package ru.yandex.practicum.filmorate.mappers;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface FilmMapper {

    Film mapToFilm(NewFilmRequest request);

    @Mapping(target = "genres", source = "genres", qualifiedByName = "sortGenres")
    FilmDto mapToFilmDto(Film film);

    static Film updateFilmFields(@NotNull Film film, @NotNull UpdateFilmRequest request) {
        Film.FilmBuilder builder = film.toBuilder();

        if (request.hasName()) {
            builder.name(request.getName());
        }

        if (request.hasDescription()) {
            builder.description(request.getDescription());
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

        if (request.hasGenres()) { //почему-то было с !
            builder.genres(request.getGenres());
        }

        if (request.hasDirectors()) {
            builder.directors(request.getDirectors());
        } else {
            builder.directors(new HashSet<>());
        }

        return builder.build();
    }

    @Named("sortGenres")
    default Set<Genre> sortGenres(Set<Genre> genres) {
        if (genres == null) return null;
        return genres.stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}