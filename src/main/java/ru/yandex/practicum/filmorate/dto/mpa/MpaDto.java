package ru.yandex.practicum.filmorate.dto.mpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class MpaDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Long id;
    private final String name;
}
