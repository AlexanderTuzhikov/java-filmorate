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
import ru.yandex.practicum.filmorate.enums.GenreName;
import ru.yandex.practicum.filmorate.enums.MpaName;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier
@Slf4j
public class FilmDbRepository extends BaseDbRepositoryImpl<Film> {
    private final GenreDbRepository genreRepository;
    private final MpaDbRepository mpaRepository;
    private final DirectorDbRepository directorDbRepository;

    public FilmDbRepository(JdbcTemplate jdbc,
                            RowMapper<Film> mapper,
                            GenreDbRepository genreRepository,
                            MpaDbRepository mpaRepository,
                            DirectorDbRepository directorDbRepository) {
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
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
            WHERE id = ?
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

    // сортировка по режиссёру

    @Language("SQL")
    private static final String SORT_FILMS_BY_YEAR_QUERY = """
            SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.id
            WHERE f.id IN (SELECT fd.film_id
                           FROM film_directors AS fd
                           WHERE fd.director_id = ?)
            ORDER BY release_date NULLS LAST
            """;

    @Language("SQL")
    private static final String SORT_FILMS_BY_LIKES_QUERY = """
            SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.id
            LEFT JOIN (
                SELECT COUNT(fl.user_id) AS likes, fl.film_id
                FROM films_likes fl
                GROUP BY fl.film_id
            ) AS l ON f.id = l.film_id
            WHERE f.id IN (SELECT fd.film_id
                           FROM film_directors fd
                           WHERE fd.director_id = ?)
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

    // общие фильмы, рекомендации, фильтры

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
                        WHERE user_id = ?
                    )
                    AND user_id != ?
                    GROUP BY user_id
                    ORDER BY COUNT(*) DESC
                    LIMIT 1
                )
                AND film_id NOT IN (
                    SELECT film_id
                    FROM films_likes
                    WHERE user_id = ?
                )
            )
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

    //  базовый JOIN для вытаскивания фильмов с жанрами и режиссёрами

    @Language("SQL")
    private static final String FIND_FILMS_WITH_RELATIONS_BASE = """
            SELECT
                f.id AS film_id,
                f.name AS film_name,
                f.description AS film_description,
                f.release_date AS film_release_date,
                f.duration AS film_duration,
                f.mpa_id AS mpa_id,
                m.name AS mpa_name,
                fg.genre_id AS genre_id,
                g.name AS genre_name,
                fd.director_id AS director_id,
                d.name AS director_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            """;

    // CRUD

    public Film save(Film film) {
        Long mpaId = film.getMpa() != null ? film.getMpa().getId() : null;

        if (mpaId != null) {
            mpaRepository.findMpa(mpaId)
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + mpaId + " не найден"));
        }

        long id = insert(
                INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId
        );

        insertFilmGenres(id, film.getGenres());
        film = film.toBuilder().id(id).build();
        directorDbRepository.saveFilmDirectors(film);

        return findById(id).orElseThrow(() -> {
            log.error("Ошибка сохранения фильма filmId={}. Фильм не найден", id);
            return new InternalServerException("Ошибка после сохранения фильм не найден");
        });
    }

    public Film update(Film film) {
        Long mpaId = film.getMpa() != null ? film.getMpa().getId() : null;

        if (mpaId != null) {
            mpaRepository.findMpa(mpaId)
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + mpaId + " не найден"));
        }

        log.info("UPDATE filmId={}, genres from request = {}",
                film.getId(),
                film.getGenres() == null ? "null" : film.getGenres().stream()
                        .map(g -> g.getId() + ":" + g.getName())
                        .toList()
        );
        log.info("UPDATE filmId={}, directors from request = {}", film.getId(), film.getDirectors());

        update(
                UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId,
                film.getId()
        );

        updateFilmGenres(film.getId(), film.getGenres());
        directorDbRepository.updateFilmDirectors(film);

        return findById(film.getId()).orElseThrow(() -> {
            log.warn("Ошибка обновления фильма filmId= {}. Фильм не найден", film.getId());
            return new NotFoundException("Ошибка после обновления — фильм не найден");
        });
    }

    public boolean delete(Long filmId) {
        return delete(DELETE_FILM_QUERY, filmId);
    }

    //  Получение фильмов

    public Optional<Film> findById(Long filmId) {
        List<Film> films = findFilmsWithRelationsByIds(List.of(filmId));
        return films.stream().findFirst();
    }

    public List<Film> findAll() {
        String sql = FIND_FILMS_WITH_RELATIONS_BASE + " ORDER BY film_id";
        return findFilmsWithRelations(sql);
    }

    public List<Film> findRecommendationsFilms(Long userId) {
        List<Film> baseFilms = jdbc.query(FIND_RECOMMENDATIONS_FILM_QUERY, mapper, userId, userId, userId);
        return enrichFilmsPreservingOrder(baseFilms);
    }

    public List<Film> searchFilms(String query, boolean byTitle, boolean byDirector) {
        String like = "%" + query.toLowerCase() + "%";

        List<Film> baseFilms;

        if (byTitle && byDirector) {
            baseFilms = jdbc.query(SEARCH_BY_TITLE_OR_DIRECTOR, mapper, like, like);
        } else if (byTitle) {
            baseFilms = jdbc.query(SEARCH_BY_TITLE, mapper, like);
        } else if (byDirector) {
            baseFilms = jdbc.query(SEARCH_BY_DIRECTOR, mapper, like);
        } else {
            return List.of();
        }

        // порядок baseFilms полностью совпадает с тем, что был до
        return enrichFilmsPreservingOrder(baseFilms);
    }

    public List<Film> findCommonFilms(long userId, long friendId) {
        List<Film> baseFilms = jdbc.query(FIND_COMMON_FILMS_SQL, mapper, userId, friendId);
        return enrichFilmsPreservingOrder(baseFilms);
    }

    public List<Long> findFilmsByYear(Long year) {
        return jdbc.queryForList(FIND_FILMS_BY_YEAR_QUERY, Long.class, year);
    }

    public List<Long> findAllFilmsByGenreId(Long genreId) {
        return jdbc.queryForList(FIND_ALL_FILMS_BY_GENRE_ID_QUERY, Long.class, genreId);
    }

    public Collection<FilmDto> getSortedFilms(Long directorId, String sort) {
        if (!directorDbRepository.containDirector(directorId)) {
            throw new NotFoundException("Режиссер с ID= " + directorId + " - не найден");
        }

        String query = SORT_FILMS_BY_LIKES_QUERY;
        if ("year".equals(sort)) {
            query = SORT_FILMS_BY_YEAR_QUERY;
        }

        // базовый список в нужном порядке (как раньше)
        List<Film> baseFilms = findMany(query, directorId);
        List<Film> filmsWithRelations = enrichFilmsPreservingOrder(baseFilms);

        return filmsWithRelations.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    //  работа с жанрами

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

    // JOIN-хелперы

    // Общий метод: выполняет SQL с JOIN и собирает Film с genres/directors/mpa

    private List<Film> findFilmsWithRelations(String sql, Object... params) {
        Map<Long, Film> films = new LinkedHashMap<>();

        jdbc.query(sql, rs -> {
            long filmId = rs.getLong("film_id");
            Film film = films.get(filmId);
            if (film == null) {
                Date releaseDate = rs.getDate("film_release_date");

                Long mpaId = rs.getLong("mpa_id");
                Mpa mpa = null;
                if (!rs.wasNull()) {
                    String mpaNameRaw = rs.getString("mpa_name");
                    MpaName mpaName = mpaNameRaw != null ? MpaName.valueOf(mpaNameRaw) : null;

                    mpa = Mpa.builder()
                            .id(mpaId)
                            .name(mpaName)
                            .build();
                }

                film = Film.builder()
                        .id(filmId)
                        .name(rs.getString("film_name"))
                        .description(rs.getString("film_description"))
                        .releaseDate(releaseDate.toLocalDate())
                        .duration(rs.getInt("film_duration"))
                        .mpa(mpa)
                        .build();

                films.put(filmId, film);
            }

            long genreId = rs.getLong("genre_id");
            if (!rs.wasNull()) {
                String genreNameRaw = rs.getString("genre_name");
                GenreName genreName = genreNameRaw != null ? GenreName.valueOf(genreNameRaw) : null;

                film.getGenres().add(
                        Genre.builder()
                                .id(genreId)
                                .name(genreName)
                                .build()
                );
            }

            long directorId = rs.getLong("director_id");
            if (!rs.wasNull()) {
                String directorName = rs.getString("director_name");
                film.getDirectors().add(
                        Director.builder()
                                .id(directorId)
                                .name(directorName)
                                .build()
                );
            }
        }, params);

        return new ArrayList<>(films.values());
    }

    // Загружает фильмы по списку id одним JOIN-запросом.

    private List<Film> findFilmsWithRelationsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String inSql = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = FIND_FILMS_WITH_RELATIONS_BASE +
                " WHERE f.id IN (" + inSql + ") ORDER BY film_id";

        return findFilmsWithRelations(sql, ids.toArray());
    }

    // Обогащает фильмы жанрами/режиссёрами/MPA, сохраняя исходный порядок списка.

    private List<Film> enrichFilmsPreservingOrder(List<Film> baseFilms) {
        if (baseFilms == null || baseFilms.isEmpty()) {
            return List.of();
        }

        List<Long> ids = baseFilms.stream()
                .map(Film::getId)
                .toList();

        List<Film> filmsWithRelations = findFilmsWithRelationsByIds(ids);

        Map<Long, Film> filmById = filmsWithRelations.stream()
                .collect(Collectors.toMap(
                        Film::getId,
                        f -> f,
                        (f1, f2) -> f1,
                        LinkedHashMap::new
                ));

        return baseFilms.stream()
                .map(f -> filmById.getOrDefault(f.getId(), f))
                .toList();
    }
}