package ru.yandex.practicum.filmorate.dal.memory;

import java.util.List;

public interface BaseInMemoryRepository<T> {

    T create(T item);

    T update(T item);

    T get(Long id);

    List<T> getAll();

    void delete(Long id);
}
