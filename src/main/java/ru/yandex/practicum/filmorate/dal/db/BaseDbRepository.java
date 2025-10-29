package ru.yandex.practicum.filmorate.dal.db;

import java.util.List;
import java.util.Optional;

public interface BaseDbRepository<T> {
    long insert(String query, Object... params);

    void update(String query, Object... params);

    Optional<T> findOne(String query, Object... params);

    List<T> findMany(String query, Object... params);

    boolean delete(String query, Long id);
}
