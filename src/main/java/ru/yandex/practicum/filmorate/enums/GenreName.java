package ru.yandex.practicum.filmorate.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum GenreName {
    @JsonProperty("Комедия")
    COMEDY("Комедия"),
    @JsonProperty("Драма")
    DRAMA("Драма"),
    @JsonProperty("Мультфильм")
    CARTOON("Мультфильм"),
    @JsonProperty("Триллер")
    THRILLER("Триллер"),
    @JsonProperty("Документальный")
    DOCUMENTARY("Документальный"),
    @JsonProperty("Боевик")
    ACTION("Боевик");

    final String dtoName;

    GenreName(String dtoName) {
        this.dtoName = dtoName;
    }
}
