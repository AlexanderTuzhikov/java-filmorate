package ru.yandex.practicum.filmorate.dto.film;

import lombok.Data;
import ru.yandex.practicum.filmorate.enums.MpaName;

import java.time.LocalDate;

@Data
public class NewFilmRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private MpaName mpaName;
}
