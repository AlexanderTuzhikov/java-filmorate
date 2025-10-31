package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.GenreName;

@Data
@Builder(toBuilder = true)
public class Genre {
    private final Long id;
    private final GenreName name;
}
