package ru.yandex.practicum.filmorate.dal.memory;

import java.util.List;

@Deprecated
public interface BaseInMemoryRepository<T> {
    @Deprecated
    T create(T item);

    @Deprecated
    T update(T item);

    @Deprecated
    T get(Long id);

    @Deprecated
    List<T> getAll();

    @Deprecated
    void delete(Long id);
}
