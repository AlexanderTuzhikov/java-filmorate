package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.MpaName;

@Data
@Builder(toBuilder = true)
public class Mpa {
    private final Long id;
    private final MpaName name;
}
