package ru.yandex.practicum.filmorate.dal.db.genre;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class GenreDbRepository extends BaseDbRepositoryImpl<Genre> {

    public GenreDbRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Language("SQL")
    private static final String FIND_GENRE_QUERY = """
            SELECT *
            FROM genres
            WHERE id = ?
            """;
    @Language("SQL")
    private static final String FIND_ALL_GENRE_QUERY = """
            SELECT *
            FROM genres
            """;
    @Language("SQL")
    private static final String FIND_FILM_GENRE_QUERY = """
            SELECT *
            FROM genres
            WHERE id IN (SELECT fg.genre_id FROM film_genres AS fg WHERE film_id = ?)
            """;

    public Optional<Genre> findGenre(Long genreId) {
        return findOne(FIND_GENRE_QUERY, genreId);
    }

    public List<Genre> findAllGenre() {
        return findMany(FIND_ALL_GENRE_QUERY);
    }

    public List<Genre> findFilmGenre(Long filmId) {
        return jdbc.query(FIND_FILM_GENRE_QUERY, mapper, filmId);
    }

}
