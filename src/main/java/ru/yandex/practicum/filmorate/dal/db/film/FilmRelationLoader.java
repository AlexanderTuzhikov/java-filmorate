package ru.yandex.practicum.filmorate.dal.db.film;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.enums.GenreName;
import ru.yandex.practicum.filmorate.enums.MpaName;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Qualifier
@Slf4j
public class FilmRelationLoader extends BaseDbRepositoryImpl<Film> {

    public FilmRelationLoader(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Language("SQL")
    public static final String FIND_FILMS_WITH_RELATIONS_BASE = """
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

    protected List<Film> findFilmsWithRelations(String sql, Object... params) {
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

    protected List<Film> findFilmsWithRelationsByIds(List<Long> ids) {
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

    protected List<Film> enrichFilmsPreservingOrder(List<Film> baseFilms) {
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
