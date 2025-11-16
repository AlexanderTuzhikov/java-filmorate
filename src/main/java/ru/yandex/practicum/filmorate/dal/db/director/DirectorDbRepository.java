package ru.yandex.practicum.filmorate.dal.db.director;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.exception.NotFoundDirector;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Slf4j
public class DirectorDbRepository extends BaseDbRepositoryImpl<Director> {

    public DirectorDbRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Language("SQL")
    private static final String FIND_ALL_DIRECTOR_QUERY = """
            SELECT *
            FROM directors
            """;
    @Language("SQL")
    private static final String FIND_FILM_DIRECTOR_QUERY = """
            SELECT *
            FROM directors
            WHERE id IN (SELECT fd.director_id FROM film_directors AS fd WHERE film_id = ?)
            """;
    @Language("SQL")
    private static final String FIND_DIRECTOR_QUERY = """
            SELECT *
            FROM directors
            WHERE id = ?
            """;
    @Language("SQL")
    private static final String INSERT_DIRECTOR_QUERY = """
            INSERT INTO directors (name)
            VALUES (?)
            """;
    @Language("SQL")
    private static final String UPDATE_DIRECTOR_QUERY = """
            UPDATE directors
            SET name = ?
            WHERE id = ?
            """;
    @Language("SQL")
    private static final String DELETE_DIRECTOR_QUERY = """
            DELETE
            FROM directors
            WHERE id = ?
            """;
    @Language("SQL")
    private static final String DELETE_ALL_DIRECTORS_QUERY = """
            DELETE
            FROM film_directors
            WHERE film_id = ?
            """;
    @Language("SQL")
    private static final String INSERT_FILM_DIRECTORS_QUERY = """
            INSERT INTO film_directors (film_id, director_id)
            VALUES (?, ?)
            """;

    public List<Director> findAllDirector() {
        return findMany(FIND_ALL_DIRECTOR_QUERY);
    }

    public List<Director> findFilmDirector(Long filmId) {
        return jdbc.query(FIND_FILM_DIRECTOR_QUERY, mapper, filmId);
    }

    public Optional<Director> findDirector(Long directorId) {
        if (!containDirector(directorId)) {
            throw new NotFoundDirector("Режиссер с ID= " + directorId + " - не найден");
        }
        return findOne(FIND_DIRECTOR_QUERY, directorId);
    }

    public void addDirector(Director director) {
        long id = insert(INSERT_DIRECTOR_QUERY, director.getName());
        director.setId(id);
    }

    public void updateDirector(Director director) {
        update(UPDATE_DIRECTOR_QUERY, director.getName(), director.getId());
    }

    public void removeDirector(Long directorId) {
        if (!containDirector(directorId)) {
            throw new NotFoundDirector("Режиссер с ID= " + directorId + " - не найден");
        }
        delete(DELETE_DIRECTOR_QUERY, directorId);
    }

    public void removeALLDirectors(Film film) {
        delete(DELETE_ALL_DIRECTORS_QUERY, film.getId());
    }

    public boolean containDirector(Long directorId) {
        Optional<Director> director = findOne(FIND_DIRECTOR_QUERY, directorId);
        return director.isPresent();
    }

    public void addDirectorsToFilm(Long filmId, Set<Director> directors) {
        jdbc.batchUpdate(INSERT_FILM_DIRECTORS_QUERY, directors.stream()
                .map(director -> new Object[]{filmId, director.getId()})
                .toList());
    }

    public void saveFilmDirectors(Film film) {
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            addDirectorsToFilm(film.getId(), film.getDirectors());
        }
    }

    public void updateFilmDirectors(Film film) {
        removeALLDirectors(film);
        saveFilmDirectors(film);
    }

}
