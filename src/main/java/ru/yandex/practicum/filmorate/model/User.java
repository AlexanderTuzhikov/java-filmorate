package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class User {
    private final long id;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Не верная электронная почта")
    private final String email;
    @NotBlank(message = "Логин не может быть пустым")
    private final String login;
    private final String name;
    @NotNull(message = "Дата рождения не может быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    private final LocalDate birthday;
}
