package ru.yandex.practicum.filmorate.dal.memory;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundUser;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserInMemoryRepository implements BaseInMemoryRepository<User> {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(@NotNull User user) {
        user = user.toBuilder()
                .id(setId())
                .build();
        log.info("Создан новый пользователь: {}", user);
        saveUser(user);

        return users.get(user.getId());
    }

    @Override
    public User update(@NotNull User user) {
        if (users.containsKey(user.getId())) {
            User existing = get(user.getId());
            user = mergeUserData(existing, user);
            log.info("Обновлен пользователь: {}", user);
        } else {
            log.warn("Попытка обновить не существующего пользователя с id={}", user.getId());
            throw new NotFoundUser("Пользователь с id=" + user.getId() + " не найден");
        }
        saveUser(user);
        return users.get(user.getId());
    }

    @Override
    public User get(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            log.warn("Попытка получить не существующего пользователя с id={}", id);
            throw new NotFoundUser("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public List<User> getAll() {
        if (users.isEmpty()) {
            log.warn("Запрошен пустой список пользователей");
            return Collections.emptyList();
        }

        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long id) {
        if (users.containsKey(id)) {
            log.info("Удален пользователь: {}", users.get(id));
            users.remove(id);
        } else {
            log.warn("Попытка удалить не существующего пользователя с id={}", id);
            throw new NotFoundUser("Пользователь с id=" + id + " не найден");
        }
    }

    private Long setId() {
        return users.keySet().stream()
                .max(Long::compareTo)
                .map(id -> id + 1)
                .orElse(1L);
    }

    private void saveUser(@NotNull User user) {
        users.put(user.getId(), user);
        log.info("Сохранен пользователь: {}", user);
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
