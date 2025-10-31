package ru.yandex.practicum.filmorate.dto.film;

import lombok.Data;
import ru.yandex.practicum.filmorate.enums.MpaName;

import java.time.LocalDate;

@Data
public class UpdateFilmRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private MpaName mpaName;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return !(releaseDate == null);
    }

    public boolean hasDuration() {
        return !(duration <= 0);
    }

    public boolean hasMpa() {
        return !(mpaName == null);
    }
}
