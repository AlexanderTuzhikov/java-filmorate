package ru.yandex.practicum.filmorate.dal.db.film;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundFilm;
import ru.yandex.practicum.filmorate.exception.NotFoundGenre;
import ru.yandex.practicum.filmorate.exception.NotFoundMpa;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier
@Slf4j
public class FilmDbRepository extends BaseDbRepositoryImpl<Film> {
    private final GenreDbRepository genreRepository;
    private final MpaDbRepository mpaRepository;

    public FilmDbRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, GenreDbRepository genreRepository,
                            MpaDbRepository mpaRepository) {
        super(jdbc, mapper);
        this.genreRepository = genreRepository;
        this.mpaRepository = mpaRepository;
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
    private static final String FIND_COMMON_FILMS_SQL = """
            SELECT f.*, COALESCE(l.likes_count, 0) AS likes_count
            FROM films f
            JOIN films_likes fl1 ON f.id = fl1.film_id AND fl1.user_id = ?
            JOIN films_likes fl2 ON f.id = fl2.film_id AND fl2.user_id = ?
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS likes_count
                FROM films_likes
                GROUP BY film_id
            ) l ON f.id = l.film_id
            ORDER BY l.likes_count DESC
            """;

    private static final String FIND_RECOMMENDATIONS_FILM_QUERY = """
            SELECT *
            FROM films
            WHERE id IN (
                SELECT film_id
                FROM films_likes
                WHERE user_id = (
                    SELECT user_id
                    FROM films_likes
                    WHERE film_id IN (
                        SELECT film_id
                        FROM films_likes
                        WHERE user_id = ?)
                    AND user_id != ?
                    GROUP BY user_id
                    ORDER BY COUNT(*) DESC
                    LIMIT 1)
            AND film_id NOT IN (
                SELECT film_id
                FROM films_likes
                WHERE user_id = ?))
            """;

    @Language("SQL")
    private static final String FIND_FILMS_BY_YEAR_QUERY = """
            SELECT f.id
            FROM films f
            WHERE EXTRACT(YEAR FROM f.release_date) = ?
            """;

    @Language("SQL")
    private static final String FIND_ALL_FILMS_BY_GENRE_ID_QUERY = """
            SELECT film_id
            FROM film_genres
            WHERE genre_id = ?
            """;

    public Film save(Film film) {
        Long mpaId = film.getMpa() != null ? film.getMpa().getId() : null;

        if (mpaId != null) {
            mpaRepository.findMpa(mpaId)
                    .orElseThrow(() -> new NotFoundMpa("Рейтинг MPA с id=" + mpaId + " не найден"));
        }

        long id = insert(INSERT_FILM_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), mpaId);
        insertFilmGenres(id, film.getGenres());
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

    public List<Film> findRecommendationsFilms(Long userId) {
        return jdbc.query(FIND_RECOMMENDATIONS_FILM_QUERY, mapper, userId, userId, userId).stream()
                .map(this::getFilm)
                .toList();
    }

    private Film getFilm(Film film) {
        Set<Genre> genres = Set.copyOf(genreRepository.findFilmGenre(film.getId()));
        Mpa mpa = Optional.ofNullable(film.getMpa())
                .map(Mpa::getId)
                .flatMap(mpaRepository::findMpa)
                .orElseThrow(() -> new NotFoundMpa("Рейтинг MPA не найден"));

        return Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(mpa)
                .genres(genres)
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

    public List<Film> findCommonFilms(long userId, long friendId) {
        return jdbc.query(FIND_COMMON_FILMS_SQL, mapper, userId, friendId)
                .stream()
                .map(this::getFilm)
                .toList();
    }

    public List<Long> findFilmsByYear(Long year) {
        return jdbc.queryForList(FIND_FILMS_BY_YEAR_QUERY, Long.class, year);
    }

    public List<Long> findAllFilmsByGenreId(Long genreId) {
        return jdbc.queryForList(FIND_ALL_FILMS_BY_GENRE_ID_QUERY, Long.class, genreId);
    }
}
