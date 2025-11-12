package ru.yandex.practicum.filmorate.dal.db.film;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.dal.db.director.DirectorDbRepository;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.*;

@Repository
@Qualifier
@Slf4j
public class FilmDbRepository extends BaseDbRepositoryImpl<Film> {
    private final GenreDbRepository genreRepository;
    private final MpaDbRepository mpaRepository;
    private final DirectorDbRepository directorDbRepository;

    public FilmDbRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, GenreDbRepository genreRepository,
                            MpaDbRepository mpaRepository, DirectorDbRepository directorDbRepository) {
        super(jdbc, mapper);
        this.genreRepository = genreRepository;
        this.mpaRepository = mpaRepository;
        this.directorDbRepository = directorDbRepository;
    }

    @Language("SQL")
    private static final String INSERT_FILM_QUERY = """
            INSERT INTO films (name, description, release_date, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;
    @Language("SQL")
    private static final String UPDATE_FILM_QUERY = """
            UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
            WHERE id = ?
            """;
    @Language("SQL")
    private static final String FIND_ONE_FILM_QUERY = """
            SELECT *
            FROM films
            WHERE id = ?;
            """;
    @Language("SQL")
    private static final String FIND_ALL_FILM_QUERY = """
            SELECT *
            FROM films
            """;
    @Language("SQL")
    private static final String DELETE_FILM_QUERY = """
            DELETE FROM films WHERE id = ?
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
    @Language("SQL")
    private static final String INSERT_DIRECTOR_TO_FILM_QUERY = """
            INSERT INTO film_directors (film_id, director_id)
            VALUES (?, ?)
            """;
    @Language("SQL")
    private static final String FIND_ALL_FILM_DIRECTOR_ID_QUERY = """
            SELECT director_id
            FROM film_directors
            WHERE film_id = ?
            """;
    @Language("SQL")
    private static final String DELETE_FILM_DIRECTOR_QUERY = """
            DELETE FROM film_directors
            WHERE film_id = ? AND director_id = ?;
            """;
    @Language("SQL")
    private static final String SORT_FILMS_BY_YEAR_QUERY = """
                    SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
                    FROM films AS f
                    JOIN mpa AS m ON f.mpa_id = m.id
                    WHERE f.id IN (SELECT fd.film_id
                                   FROM film_directors AS fd
                                   WHERE fd.director_id =?)
                    ORDER BY release_date ASC NULLS LAST""";
    @Language("SQL")
    private static final String SORT_FILMS_BY_LIKES_QUERY = """
                    SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
                    FROM films AS f
                    JOIN mpa AS m ON f.mpa_id = m.id
                    LEFT JOIN (SELECT COUNT(fl.user_id) AS likes,
                               fl.film_id
                               FROM film_likes AS fl
                               GROUP BY fl.film_id) AS l ON f.id = l.film_id
                    WHERE f.id IN (SELECT fd.film_id
                                   FROM film_directors AS fd
                                   WHERE fd.director_id = ?)
                    ORDER BY l.likes DESC NULLS LAST""";


    public Film save(Film film) {
        Long mpaId = film.getMpa() != null ? film.getMpa().getId() : null;

        if (mpaId != null) {
            mpaRepository.findMpa(mpaId)
                    .orElseThrow(() -> new NotFoundMpa("Рейтинг MPA с id=" + mpaId + " не найден"));
        }

        long id = insert(INSERT_FILM_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), mpaId);
        insertFilmGenres(id, film.getGenres());
        insertFilmDirectors(id, film.getDirectors());

        Optional<Film> savedFilm = findById(id);

        if (savedFilm.isEmpty()) {
            log.error("Ошибка сохранения фильма filmId= {}. Фильм не найден", id);
            throw new InternalServerException("Ошибка после сохранения фильм не найден");
        }

        return savedFilm.get();
    }

    public Film update(Film film) {
        Long mpaId = film.getMpa() != null ? film.getMpa().getId() : null;

        if (mpaId != null) {
            mpaRepository.findMpa(mpaId)
                    .orElseThrow(() -> new NotFoundMpa("Рейтинг MPA с id=" + mpaId + " не найден"));
        }

        update(UPDATE_FILM_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), mpaId, film.getId());
        updateFilmGenres(film.getId(), film.getGenres());
        updateFilmDirectors(film.getId(), film.getDirectors());

        Optional<Film> updateFilm = findById(film.getId());

        if (updateFilm.isEmpty()) {
            log.warn("Ошибка обновления фильма filmId= {}. Фильм не найден", film.getId());
            throw new NotFoundFilm("Ошибка после обновления — фильм не найден");
        }

        return updateFilm.get();
    }

    public boolean delete(Long filmId) {
        return delete(DELETE_FILM_QUERY, filmId);
    }

    public Optional<Film> findById(Long filmId) {
        return findOne(FIND_ONE_FILM_QUERY, filmId).map(this::getFilm);
    }

    public List<Film> findAll() {
        return findMany(FIND_ALL_FILM_QUERY).stream()
                .map(this::getFilm)
                .toList();
    }

    private Film getFilm(Film film) {
        Set<Genre> genres = Set.copyOf(genreRepository.findFilmGenre(film.getId()));
        Mpa mpa = Optional.ofNullable(film.getMpa())
                .map(Mpa::getId)
                .flatMap(mpaRepository::findMpa)
                .orElseThrow(() -> new NotFoundMpa("Рейтинг MPA не найден"));
        Set<Director> directors = Set.copyOf(directorDbRepository.findFilmDirector(film.getId()));

        return Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(mpa)
                .genres(genres)
                .directors(directors)
                .build();
    }

    private void insertFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        for (Genre genre : genres) {
            Long genreId = genre.getId();
            genreRepository.findGenre(genreId)
                    .orElseThrow(() -> new NotFoundGenre("Жанр не найден: id=" + genreId));
        }

        for (Genre genre : genres) {
            jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genre.getId());
        }
    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        for (Genre genre : genres) {
            Long genreId = genre.getId();
            genreRepository.findGenre(genreId)
                    .orElseThrow(() -> new NotFoundGenre("Жанр не найден: id=" + genreId));
        }

        List<Long> existGenre = findFilmGenresId(filmId);

        for (Long genreId : existGenre) {
            deleteFilmGenre(filmId, genreId);
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

    private void insertFilmDirectors(Long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) return;

        for (Director director : directors) {
            Long directorId = director.getId();
            directorDbRepository.findDirector(directorId)
                    .orElseThrow(() -> new NotFoundGenre("Режиссёр не найден: id=" + directorId));
        }

        for (Director director : directors) {
            jdbc.update(INSERT_DIRECTOR_TO_FILM_QUERY, filmId, director.getId());
        }

    }

    private void updateFilmDirectors(Long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) return;

        for (Director director : directors) {
            Long directorId = director.getId();
            directorDbRepository.findDirector(directorId)
                    .orElseThrow(() -> new NotFoundGenre("Режиссёр не найден: id=" + directorId));
        }

        List<Long> existDirector = findFilmDirectorsId(filmId);

        for (Long directorId : existDirector) {
            deleteFilmDirector(filmId, directorId);
        }

        for (Director director : directors) {
            jdbc.update(INSERT_DIRECTOR_TO_FILM_QUERY, filmId, director.getId());
        }
    }

    private @NotNull List<Long> findFilmDirectorsId(Long filmId) {
        return jdbc.queryForList(FIND_ALL_FILM_DIRECTOR_ID_QUERY, Long.class, filmId);
    }

    private void deleteFilmDirector(Long filmId, Long directorId) {
        int rowsDeleted = jdbc.update(DELETE_FILM_DIRECTOR_QUERY, filmId, directorId);

        if (rowsDeleted == 0) {
            log.warn("Не удалось удалить режиссера directorId= {}, filmId= {}", directorId, filmId);
        }
    }

    public Collection<Film> getSortedFilms(Long directorId, String sort) {
        if (!directorDbRepository.containDirector(directorId)) {
            throw new NotFoundDirector("Режиссер с ID= " + directorId + " - не найден");
        }

        String query = SORT_FILMS_BY_LIKES_QUERY;
        if (sort.equals("year")) {
            query = SORT_FILMS_BY_YEAR_QUERY;
        }

        List<Film> films = findMany(query, directorId);
        for (Film film : films) {
            insertFilmGenres(film.getId(), new HashSet<>());
            insertFilmDirectors(film.getId(), new HashSet<>());
        }

        return films;
    }
}
