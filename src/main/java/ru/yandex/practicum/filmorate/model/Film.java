package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class Film {
    private final long id;
    @NotBlank(message = "Название фильма не может быть пустым")
    private final String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private final String description;
    @NotNull(message = "Дата релиза не может быть пустой")
    private final LocalDate releaseDate;
    @NotNull(message = "Продолжительность фильма не может быть пустой")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private final int duration;
}
