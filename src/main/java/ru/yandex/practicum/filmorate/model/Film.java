package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.enums.GenreName;
import ru.yandex.practicum.filmorate.enums.MpaName;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Data
@Builder(toBuilder = true)
public class Film {
    private final Long id;
    @NotBlank(message = "Название фильма не может быть пустым")
    private final String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private final String description;
    @NotNull(message = "Дата релиза не может быть пустой")
    private final LocalDate releaseDate;
    @NotNull(message = "Продолжительность фильма не может быть пустой")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private final int duration;
    @NotNull
    private final MpaName mpaName;
    @Builder.Default
    private final Set<Long> likes = new HashSet<>();
    @Builder.Default
    private final Set<GenreName> genreNames = new HashSet<>();

    public void addLike(User user) {
        likes.add(user.getId());
        log.info("Пользователь: id={} поставил лайк фильму: id={}", user.getId(), this.getId());
    }

    public void removeLike(User user) {
        if (!likes.contains(user.getId())) {
            log.warn("Попытка удаления отсутствующего лайка: {}", user.getId());
        }

        likes.remove(user.getId());
        log.info("Пользователь: id={} удалил лайк фильму id={}", user.getId(), this.getId());
    }

}
