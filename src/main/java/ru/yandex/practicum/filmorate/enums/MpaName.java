package ru.yandex.practicum.filmorate.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MpaName {
    G,
    PG,
    @JsonProperty("PG-13")
    PG_13,
    R,
    @JsonProperty("NC-17")
    NC_17
}