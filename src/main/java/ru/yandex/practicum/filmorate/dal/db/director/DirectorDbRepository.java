package ru.yandex.practicum.filmorate.dal.db.director;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

    public Optional<Director> findDirector(Long directorId) {
        if (!containDirector(directorId)) {
            throw new NotFoundException("Режиссер с ID= " + directorId + " - не найден");
        }
        return findOne(FIND_DIRECTOR_QUERY, directorId);
    }

    public void addDirector(Director director) {
        long id = insert(INSERT_DIRECTOR_QUERY, director.getName());
        director.setId(id);
    }

    public Director updateDirector(Director director) {
        update(UPDATE_DIRECTOR_QUERY, director.getName(), director.getId());
        Optional<Director> updatedDirector = findDirector(director.getId());

        if (updatedDirector.isEmpty()) {
            log.warn("Ошибка обновления режиссера directorId= {}. Фильм не найден", director.getId());
            throw new NotFoundException("Ошибка после обновления — режиссер не найден не найден");
        }
        return updatedDirector.get();
    }

    public void removeDirector(Long directorId) {
        if (!containDirector(directorId)) {
            throw new NotFoundException("Режиссер с ID= " + directorId + " - не найден");
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
        if (film.getDirectors() == null) {
            return;
        }
        removeALLDirectors(film);
        if (film.getDirectors().isEmpty()) {
            return;
        }
        saveFilmDirectors(film);
    }
}
