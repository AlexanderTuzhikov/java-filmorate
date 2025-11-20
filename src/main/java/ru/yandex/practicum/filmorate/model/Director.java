package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@Builder(toBuilder = true)
public class Director {
    private Long id;
    private String name;
}
