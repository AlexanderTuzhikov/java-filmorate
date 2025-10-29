package ru.yandex.practicum.filmorate.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class UserDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Long id;
    private final String email;
    private final String login;
    private final String name;
    private final LocalDate birthday;
}
