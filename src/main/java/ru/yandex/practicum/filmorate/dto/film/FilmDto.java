package ru.yandex.practicum.filmorate.dto.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.MpaName;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class FilmDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Long id;
    private final String name;
    private final String description;
    private final LocalDate releaseDate;
    private final int duration;
    private final MpaName mpaName;
}
