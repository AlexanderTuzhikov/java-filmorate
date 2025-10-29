package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<Long, FriendshipStatus> friends = new HashMap<>();

    public void addFriend(User user, FriendshipStatus status) {
        friends.put(user.getId(), status);
        log.info("Пользователь: id={} добавлен в друзья id={}. Статус дружбы: status={}", user.getId(), this.getId(), status);
    }

    public void removeFriend(User user) {
        if (!friends.containsKey(user.getId())) {
            log.warn("Попытка удаления из друзей: id={} не является другом id={}", user.getId(), this.getId());
        }

        friends.remove(user.getId());
        log.info("Пользователь: id={} удален из друзей id={}", user.getId(), this.getId());
    }

}
