package ru.yandex.practicum.filmorate.dal.db.film;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.dal.db.director.DirectorDbRepository;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier
@Slf4j
public class FilmDbRepository extends BaseDbRepositoryImpl<Film> {
    private final GenreDbRepository genreRepository;
    private final MpaDbRepository mpaRepository;
    private final DirectorDbRepository directorDbRepository;
    private final FilmRelationLoader filmRelationLoader;

    public FilmDbRepository(JdbcTemplate jdbc,
                            RowMapper<Film> mapper,
                            GenreDbRepository genreRepository,
                            MpaDbRepository mpaRepository,
                            DirectorDbRepository directorDbRepository, FilmRelationLoader filmRelationLoader) {
        super(jdbc, mapper);
        this.genreRepository = genreRepository;
        this.mpaRepository = mpaRepository;
        this.directorDbRepository = directorDbRepository;
        this.filmRelationLoader = filmRelationLoader;
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

        genreRepository.insertFilmGenres(id, film.getGenres());
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

        genreRepository.updateFilmGenres(film.getId(), film.getGenres());
        directorDbRepository.updateFilmDirectors(film);

        return findById(film.getId()).orElseThrow(() -> {
            log.warn("Ошибка обновления фильма filmId= {}. Фильм не найден", film.getId());
            return new NotFoundException("Ошибка после обновления — фильм не найден");
        });
    }

    public boolean delete(Long filmId) {
        return delete(DELETE_FILM_QUERY, filmId);
    }

    public Optional<Film> findById(Long filmId) {
        List<Film> films = filmRelationLoader.findFilmsWithRelationsByIds(List.of(filmId));
        return films.stream().findFirst();
    }

    public List<Film> findAll() {
        String sql = filmRelationLoader.FIND_FILMS_WITH_RELATIONS_BASE + " ORDER BY film_id";
        return filmRelationLoader.findFilmsWithRelations(sql);
    }
}