package ru.yandex.practicum.filmorate.dal.db.film;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

@Repository
@Qualifier
@Slf4j
public class FilmDbSearcher extends BaseDbRepositoryImpl<Film> {
    private final FilmRelationLoader filmRelationLoader;

    public FilmDbSearcher(JdbcTemplate jdbc,
                          RowMapper<Film> mapper,
                          FilmRelationLoader filmRelationLoader) {
        super(jdbc, mapper);
        this.filmRelationLoader = filmRelationLoader;
    }

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
    @Language("SQL")
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

        return filmRelationLoader.enrichFilmsPreservingOrder(baseFilms);
    }

    public Collection<FilmDto> getSortedFilms(Long directorId, String sort) {
        String query = SORT_FILMS_BY_LIKES_QUERY;

        if ("year".equals(sort)) {
            query = SORT_FILMS_BY_YEAR_QUERY;
        }

        List<Film> baseFilms = findMany(query, directorId);
        List<Film> filmsWithRelations = filmRelationLoader.enrichFilmsPreservingOrder(baseFilms);

        return filmsWithRelations.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<Film> findRecommendationsFilms(Long userId) {
        List<Film> baseFilms = jdbc.query(FIND_RECOMMENDATIONS_FILM_QUERY, mapper, userId, userId, userId);
        return filmRelationLoader.enrichFilmsPreservingOrder(baseFilms);
    }

    public List<Film> findCommonFilms(long userId, long friendId) {
        List<Film> baseFilms = jdbc.query(FIND_COMMON_FILMS_SQL, mapper, userId, friendId);
        return filmRelationLoader.enrichFilmsPreservingOrder(baseFilms);
    }

    public List<Long> findFilmsByYear(Long year) {
        return jdbc.queryForList(FIND_FILMS_BY_YEAR_QUERY, Long.class, year);
    }

    public List<Long> findAllFilmsByGenreId(Long genreId) {
        return jdbc.queryForList(FIND_ALL_FILMS_BY_GENRE_ID_QUERY, Long.class, genreId);
    }
}
