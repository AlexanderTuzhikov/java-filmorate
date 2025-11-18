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
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
    private static final String SORT_FILMS_BY_YEAR_QUERY = """
            SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.id
            WHERE f.id IN (SELECT fd.film_id
                           FROM film_directors AS fd
                           WHERE fd.director_id =?)
            ORDER BY release_date NULLS LAST
            """;
    @Language("SQL")
    private static final String SORT_FILMS_BY_LIKES_QUERY = """
            SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.id
            LEFT JOIN (SELECT COUNT(fl.user_id) AS likes, fl.film_id
                       FROM films_likes fl
                       GROUP BY fl.film_id) AS l ON f.id = l.film_id
            WHERE f.id IN (SELECT fd.film_id
                           FROM film_directors fd
                           WHERE fd.director_id =?)
            ORDER BY l.likes DESC NULLS LAST
            """;
    @Language("SQL")
    private static final String SEARCH_BY_TITLE_OR_DIRECTOR = """
            SELECT f.*, COUNT(fl.user_id) AS likes
            FROM films f
            LEFT JOIN films_likes fl ON f.id = fl.film_id
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            WHERE LOWER(f.name) LIKE ?
               OR LOWER(d.name) LIKE ?
            GROUP BY f.id
            ORDER BY likes DESC
            """;
    @Language("SQL")
    private static final String SEARCH_BY_TITLE = """
            SELECT f.*, COUNT(fl.user_id) AS likes
            FROM films f
            LEFT JOIN films_likes fl ON f.id = fl.film_id
            WHERE LOWER(f.name) LIKE ?
            GROUP BY f.id
            ORDER BY likes DESC
            """;

    @Language("SQL")
    private static final String SEARCH_BY_DIRECTOR = """
            SELECT f.*, COUNT(fl.user_id) AS likes
            FROM films f
            LEFT JOIN films_likes fl ON f.id = fl.film_id
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            WHERE LOWER(d.name) LIKE ?
            GROUP BY f.id
            ORDER BY likes DESC
            """;
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
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + mpaId + " не найден"));
        }

        long id = insert(INSERT_FILM_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), mpaId);
        insertFilmGenres(id, film.getGenres());
        film = film.toBuilder().id(id).build();

        directorDbRepository.saveFilmDirectors(film);

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
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + mpaId + " не найден"));
        }
        log.info("UPDATE filmId={}, genres from request = {}",
                film.getId(), film.getGenres() == null ? "null" : film.getGenres().stream()
                        .map(g -> g.getId() + ":" + g.getName())
                        .toList()
        );
        log.info("UPDATE filmId={}, directors from request = {}", film.getId(), film.getDirectors());


        update(UPDATE_FILM_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), mpaId, film.getId());

        updateFilmGenres(film.getId(), film.getGenres());
        directorDbRepository.updateFilmDirectors(film);

        Optional<Film> updateFilm = findById(film.getId());

        if (updateFilm.isEmpty()) {
            log.warn("Ошибка обновления фильма filmId= {}. Фильм не найден", film.getId());
            throw new NotFoundException("Ошибка после обновления — фильм не найден");
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

        List<Director> directorsFromDb = directorDbRepository.findFilmDirector(film.getId());
        log.info("getFilm: filmId={}, directors from DB = {}",
                film.getId(),
                directorsFromDb.stream()
                        .map(d -> d.getId() + ":" + d.getName())
                        .toList()
        );


        Set<Genre> genres = new HashSet<>(genreRepository.findFilmGenre(film.getId()));
        Mpa mpa = Optional.ofNullable(film.getMpa())
                .map(Mpa::getId)
                .flatMap(mpaRepository::findMpa)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA не найден"));

        Set<Director> directors = new HashSet<>(directorDbRepository.findFilmDirector(film.getId()));

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
                    .orElseThrow(() -> new NotFoundException("Жанр не найден: id=" + genreId));
        }

        for (Genre genre : genres) {
            jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genre.getId());
        }
    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
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
            genreRepository.findGenre(genreId)
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

    public Collection<FilmDto> getSortedFilms(Long directorId, String sort) {
        if (!directorDbRepository.containDirector(directorId)) {
            throw new NotFoundException("Режиссер с ID= " + directorId + " - не найден");
        }

        String query = SORT_FILMS_BY_LIKES_QUERY;
        if ("year".equals(sort)) {
            query = SORT_FILMS_BY_YEAR_QUERY;
        }

        List<FilmDto> films = findMany(query, directorId).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
        for (FilmDto film : films) {
            addGenresAndDirectorsToFilm(film);
        }
        return films;
    }

    private void addGenresAndDirectorsToFilm(FilmDto film) {
        if (film != null) {
            List<Genre> genres = genreRepository.findFilmGenre(film.getId());
            genres.sort(Comparator.comparing(Genre::getId));
            film.setGenres(new LinkedHashSet<>(genres));
            film.setDirectors(new HashSet<>(directorDbRepository.findFilmDirector(film.getId())));

            if (film.getMpa() != null && film.getMpa().getId() != null) {
                Mpa fullMpa = mpaRepository.findMpa(film.getMpa().getId()).orElse(null);
                film.setMpa(fullMpa);
            }
        }
    }

    public List<Film> searchFilms(String query, boolean byTitle, boolean byDirector) {
        String like = "%" + query.toLowerCase() + "%";

        List<Film> films;

        if (byTitle && byDirector) {
            films = jdbc.query(SEARCH_BY_TITLE_OR_DIRECTOR, mapper, like, like);
        } else if (byTitle) {
            films = jdbc.query(SEARCH_BY_TITLE, mapper, like);
        } else if (byDirector) {
            films = jdbc.query(SEARCH_BY_DIRECTOR, mapper, like);
        } else {
            return List.of();
        }


        return films.stream()
                .map(this::getFilm)
                .toList();
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
