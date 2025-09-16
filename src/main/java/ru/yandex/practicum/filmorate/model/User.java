package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class User {
    private final long id;
    private final String email;
    private final String login;
    private final String name;
    private final LocalDate birthday;
}
