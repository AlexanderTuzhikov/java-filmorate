package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundUser;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public User saveUser(@NotNull User user) {
        if (user.getId() == null) {
            user = user.toBuilder()
                    .id(setId())
                    .build();
            log.info("Добавлен новый пользователь: {}", user);
        } else if (users.containsKey(user.getId())) {
            User existing = getUser(user.getId());
            user = mergeUserData(existing, user);
            log.info("Обновлен пользователь: {}", user);
        } else {
            log.warn("Попытка обновить не существующего пользователя с id={}", user.getId());
            throw new NotFoundUser("Пользователь с id=" + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
        return users.get(user.getId());
    }

    public User getUser(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            log.warn("Попытка получить не существующего пользователя с id={}", id);
            throw new NotFoundUser("Пользователь с id=" + id + " не найден");
        }
    }

    public List<User> getAllUsers() {
        if (users.isEmpty()) {
            log.warn("Запрошен пустой список пользователей");
            return Collections.emptyList();
        }

        return new ArrayList<>(users.values());
    }

    public void deleteUser(Long id) {
        if (users.containsKey(id)) {
            log.info("Удален пользователь: {}", users.get(id));
            users.remove(id);
        } else {
            log.warn("Попытка удалить не существующего пользователя с id={}", id);
            throw new NotFoundUser("Пользователь с id=" + id + " не найден");
        }
    }

    public void deleteAllUsers() {
        log.info("Список пользователей очищен({}шт.)", users.size());
        users.clear();
    }

    private Long setId() {
        return users.keySet().stream()
                .max(Long::compareTo)
                .map(id -> id + 1)
                .orElse(1L);
    }

    private User mergeUserData(User existing, User newData) {
        return newData.toBuilder()
                .id(newData.getId())
                .email(newData.getEmail())
                .login(newData.getLogin())
                .name(newData.getName())
                .birthday(newData.getBirthday())
                .friends(existing.getFriends())
                .build();
    }
}
