package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
    private final Mpa mpa;
    @Builder.Default
    private final Set<Long> likes = new HashSet<>();
    @Builder.Default
    private final Set<Genre> genres = new HashSet<>();

    @Deprecated
    public void addLike(User user) {
        likes.add(user.getId());
        log.info("Пользователь: id={} поставил лайк фильму: id={}", user.getId(), this.getId());
    }

    @Deprecated
    public void removeLike(User user) {
        if (!likes.contains(user.getId())) {
            log.warn("Попытка удаления отсутствующего лайка: {}", user.getId());
        }

        likes.remove(user.getId());
        log.info("Пользователь: id={} удалил лайк фильму id={}", user.getId(), this.getId());
    }
}
