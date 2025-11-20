package ru.yandex.practicum.filmorate.dbTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import ru.yandex.practicum.filmorate.dal.db.director.DirectorDbRepository;
import ru.yandex.practicum.filmorate.dal.db.director.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.db.film.FilmDbRepository;
import ru.yandex.practicum.filmorate.dal.db.film.FilmRelationLoader;
import ru.yandex.practicum.filmorate.dal.db.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbRepository.class, GenreDbRepository.class, MpaDbRepository.class, DirectorDbRepository.class,
        FilmRelationLoader.class, FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class, DirectorRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbRepositoryTest {
    private final GenreDbRepository genreRepository;
    private final FilmDbRepository filmRepository;

    @Test
    @DisplayName("Найти Genre по Id в БД")
    public void testFindGenre() {
        Optional<Genre> genre = genreRepository.findGenre(1L);

        Assert.isTrue(genre.isPresent(), "Genre не вернулся");
    }

    @Test
    @DisplayName("Найти список всех Genre в БД")
    public void testFindAllGenre() {
        List<Genre> genres = genreRepository.findAllGenre();

        Assert.notEmpty(genres, "Список всех Genre не вернулся");
    }

    @Test
    @DisplayName("Найти список Genre фильма в БД")
    public void testFindFilmGenre() {
        Film film = filmRepository.save(DataTest.TEST_FILM);
        Long filmId = film.getId();

        List<Genre> genres = genreRepository.findFilmGenre(filmId);

        Assert.notEmpty(genres, "Список Genre фильм не вернулся");
    }







}

