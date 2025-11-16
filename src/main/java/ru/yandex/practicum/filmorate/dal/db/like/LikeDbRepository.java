package ru.yandex.practicum.filmorate.dal.db.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.film.FilmDbRepository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class LikeDbRepository {
    private final FilmDbRepository filmDbRepository;
    private final JdbcTemplate jdbc;

    @Language("SQL")
    private static final String INSERT_LIKE_QUERY = """
            INSERT INTO films_likes (film_id, user_id)
            VALUES (?, ?)
            """;
    @Language("SQL")
    private static final String DELETE_LIKE_QUERY = """
            DELETE FROM films_likes
            WHERE film_id = ? AND user_id = ?
            """;
    @Language("SQL")
    private static final String FIND_FILM_LIKES_QUERY = """
            SELECT user_id FROM films_likes
            WHERE film_id = ?
            """;
    @Language("SQL")
    private static final String FIND_POPULAR_FILMS_QUERY = """
            SELECT f.id AS film_id
                   FROM films AS f
                   LEFT JOIN films_likes AS fl ON f.id = fl.film_id
                   GROUP BY f.id
                   ORDER BY COUNT(fl.user_id) DESC
            """;

    public void save(Long filmId, Long userId) {
        int rowsSaved = jdbc.update(INSERT_LIKE_QUERY, filmId, userId);

        if (rowsSaved == 0) {
            log.warn("Ошибка при сохранении лайка фильму id= {}, от пользователя id= {}", filmId, userId);
        }
    }

    public void delete(Long filmId, Long userId) {
        int rowsSaved = jdbc.update(DELETE_LIKE_QUERY, filmId, userId);

        if (rowsSaved == 0) {
            log.warn("Ошибка при удалении лайка фильму id= {}, от пользователя id= {}", filmId, userId);
        }
    }

    public List<Long> findFilmLikes(Long filmId) {
        return jdbc.queryForList(FIND_FILM_LIKES_QUERY, Long.class, filmId);
    }

    public List<Long> findPopularFilms() {
        return jdbc.queryForList(FIND_POPULAR_FILMS_QUERY, Long.class);
    }

}
