package ru.yandex.practicum.filmorate.dto.director;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder(toBuilder = true)
public class DirectorDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String name;
}
