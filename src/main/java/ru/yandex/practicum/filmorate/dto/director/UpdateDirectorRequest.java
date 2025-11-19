package ru.yandex.practicum.filmorate.dto.director;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDirectorRequest {
    @NotNull(message = "Id не может быть пустой")
    private Long id;
    @NotBlank(message = "Имя режиссера не может быть пустым")
    private String name;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }
}
