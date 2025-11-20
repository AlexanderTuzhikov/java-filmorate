package ru.yandex.practicum.filmorate.dal.db.genre;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    @Language("SQL")
    private static final String INSERT_FILM_GENRE_QUERY = """
            INSERT INTO film_genres (film_id, genre_id)
            VALUES (?, ?);
            """;
    @Language("SQL")
    private static final String FIND_ALL_FILM_GENRE_ID_QUERY = """
            SELECT genre_id
            FROM film_genres AS fg
            WHERE film_id = ?
            """;
    @Language("SQL")
    private static final String DELETE_FILM_GENRE_QUERY = """
            DELETE FROM film_genres
            WHERE film_id = ? AND genre_id = ?;
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

    public void insertFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        for (Genre genre : genres) {
            Long genreId = genre.getId();
            findGenre(genreId)
                    .orElseThrow(() -> new NotFoundException("Жанр не найден: id=" + genreId));
        }

        for (Genre genre : genres) {
            jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genre.getId());
        }
    }

    public void updateFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null) {
            return;
        }
        List<Long> existGenre = findFilmGenresId(filmId);
        for (Long genreId : existGenre) {
            deleteFilmGenre(filmId, genreId);
        }
        if (genres.isEmpty()) {
            return;
        }

        for (Genre genre : genres) {
            Long genreId = genre.getId();
            findGenre(genreId)
                    .orElseThrow(() -> new NotFoundException("Жанр не найден: id=" + genreId));
        }

        for (Genre genre : genres) {
            jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genre.getId());
        }
    }

    private void deleteFilmGenre(Long filmId, Long genreId) {
        int rowsDeleted = jdbc.update(DELETE_FILM_GENRE_QUERY, filmId, genreId);

        if (rowsDeleted == 0) {
            log.warn("Не удалось удалить жанр genreId= {}, filmId= {}", genreId, filmId);
        }
    }

    private @NotNull List<Long> findFilmGenresId(Long filmId) {
        return jdbc.queryForList(FIND_ALL_FILM_GENRE_ID_QUERY, Long.class, filmId);
    }
}
