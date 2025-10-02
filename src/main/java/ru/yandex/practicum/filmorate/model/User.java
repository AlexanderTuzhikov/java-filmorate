package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Data
@Builder(toBuilder = true)
public class User {
    private final Long id;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Не верная электронная почта")
    private final String email;
    @NotBlank(message = "Логин не может быть пустым")
    private final String login;
    private final String name;
    @NotNull(message = "Дата рождения не может быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    private final LocalDate birthday;
    @Builder.Default
    private final Set<Long> friends = new HashSet<>();

    public void addFriend(User user) {
        friends.add(user.getId());
        log.info("Пользователь: id={} добавлен в друзья id={}", user.getId(), this.getId());
    }

    public void removeFriend(User user) {
        if (!friends.contains(user.getId())) {
            log.warn("Попытка удаления из друзей: id={} не является другом id={}", user.getId(), this.getId());
        }

        friends.remove(user.getId());
        log.info("Пользователь: id={} удален из друзей id={}", user.getId(), this.getId());
    }

}
