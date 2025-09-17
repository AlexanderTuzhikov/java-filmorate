package ru.yandex.practicum.filmorate.validationTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.time.LocalDate;
import java.util.Set;


public class FilmValidatorTest {
    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Test
    @DisplayName("Film name empty не проходит валидацию")
    void setFailName() {
        //Given
        Film invalidFilm = Film.builder()
                .description("Test description")
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();
        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        //Then
        assertFalse(violations.isEmpty(), "Валидация по name фильма не работает");
    }

    @Test
    @DisplayName("Film description > 200 символов не проходит валидацию")
    void setDescriptionSize201() {
        //Given
        Film invalidFilm = Film.builder()
                .name("Test film")
                .description("А".repeat(201))
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();
        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        //Then
        assertFalse(violations.isEmpty(), "Валидация по size description фильма не работает");
    }

    @Test
    @DisplayName("Film releaseDate null не проходит валидацию")
    void setFailReleaseDate() {
        //Given
        Film invalidFilm = Film.builder()
                .name("Test film")
                .description("Test description")
                .duration(120)
                .build();
        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        //Then
        assertFalse(violations.isEmpty(), "Валидация по null releaseDate не работает");
    }

    @Test
    @DisplayName("Film releaseDate before 28.12.1895 не проходит валидацию")
    void setReleaseDateBeforeValidDate() {
        //Given
        final LocalDate VALID_RELEASE_DATE = LocalDate.of(1895, 12, 28);
        //When
        Film invalidFilm = Film.builder()
                .name("Test film")
                .description("Test description")
                .releaseDate(VALID_RELEASE_DATE.minusDays(1))
                .duration(120)
                .build();
        //Then
        assertThrows(ValidationException.class, () -> FilmValidator.filmValid(invalidFilm),
                "Валидация с releaseDate before 28.12.1895 фильма не работает");
    }

    @Test
    @DisplayName("Film duration negative не проходит валидацию")
    void setNegativeDuration() {
        //Given
        Film invalidFilm = Film.builder()
                .name("Test film")
                .description("Test description")
                .releaseDate(LocalDate.now())
                .duration(-120)
                .build();
        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        //Then
        assertFalse(violations.isEmpty(), "Валидация negative duration фильма не работает");
    }

    @Test
    @DisplayName("Film duration null не проходит валидацию")
    void setFailDuration() {
        //Given
        Film invalidFilm = Film.builder()
                .name("Test film")
                .description("Test description")
                .releaseDate(LocalDate.now())
                .build();
        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        //Then
        assertFalse(violations.isEmpty(), "Валидация null duration фильма не работает");
    }
}
