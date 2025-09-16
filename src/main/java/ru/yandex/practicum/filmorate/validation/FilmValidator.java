package ru.yandex.practicum.filmorate.validation;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmValidator {
    private static final Logger log = LoggerFactory.getLogger(FilmValidator.class);
    private static final int MAX_LENGTH_DESCRIPTION = 200;
    private static final LocalDate RELEASE_DATE_CONTROL = LocalDate.of(1895, 12, 28);

    public static void filmValidator(@NotNull Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Передано пустое название фильма: {}", film);
            throw new ValidationException("Название фильма не может быть пустым;");
        }

        if (film.getDescription().length() > MAX_LENGTH_DESCRIPTION) {
            log.warn("Максимальная длина описания — 200 символов; Передано описание: {} символов",
                    film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов;");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(RELEASE_DATE_CONTROL)) {
            log.warn("Дата релиза — не раньше 28 декабря 1895 года; Передана дата релиза: {}",
                    film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года;");
        }

        if (film.getDuration() == 0 || film.getDuration() < 0) {
            log.warn("Продолжительность фильма должна быть положительным числом; Передана продолжительность фильма: {}",
                    film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }
}
