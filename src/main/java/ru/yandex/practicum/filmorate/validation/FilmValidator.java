package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
public class FilmValidator {
    private static final LocalDate RELEASE_DATE_CONTROL = LocalDate.of(1895, 12, 28);

    public static Film filmValid(@NotNull Film film) {
        return Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(releaseDateValid(film))
                .duration(film.getDuration())
                .mpa(film.getMpa())
                .genres(film.getGenres())
                .directors(film.getDirectors())
                .build();
    }

    private static LocalDate releaseDateValid(@NotNull Film film) {
        if (film.getReleaseDate().isBefore(RELEASE_DATE_CONTROL)) {
            log.warn("Дата релиза — не раньше 1895-12-28; Передана дата релиза: {}",
                    film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 1895-12-28;");
        }

        return film.getReleaseDate();
    }

}
