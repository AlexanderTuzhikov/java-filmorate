package ru.yandex.practicum.filmorate.dal.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;


@Repository
@Qualifier
@Slf4j
public class FilmDbRepository extends BaseDbRepositoryImpl<Film> {

    public FilmDbRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }
}
