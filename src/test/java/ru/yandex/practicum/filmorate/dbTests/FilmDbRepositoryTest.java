package ru.yandex.practicum.filmorate.dbTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
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
import ru.yandex.practicum.filmorate.dal.db.film.FilmDbSearcher;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.db.like.LikeDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbRepository.class, GenreDbRepository.class, MpaDbRepository.class, DirectorDbRepository.class,
        FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class, DirectorRowMapper.class, UserDbRepository.class,
        UserRowMapper.class, FilmRelationLoader.class, FilmDbSearcher.class, LikeDbRepository.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbRepositoryTest {
    private final FilmDbRepository filmRepository;
    private final UserDbRepository userRepository;
    private final LikeDbRepository likeRepository;
    private final FilmDbSearcher filmSearch;

    private Film film;
    private Long filmId;
    private Film otherFilm;
    private Long otherFilmId;
    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        film = filmRepository.save(DataTest.TEST_FILM);
        filmId = film.getId();
        otherFilm = filmRepository.save(DataTest.OTHER_TEST_FILM);
        otherFilmId = otherFilm.getId();
        User user = userRepository.save(DataTest.TEST_USER);
        userId = user.getId();
        User otherUser = userRepository.save(DataTest.OTHER_TEST_USER);
        otherUserId = otherUser.getId();
    }

    @Test
    @DisplayName("Сохранение нового фильма в БД")
    public void testSaveFilm() {
        Assert.notNull(film, "Вернулся NULL при сохранении");
        Assert.notNull(filmId, "ID не присвоен при сохранении нового фильма");
        Assert.notNull(film.getName(), "NAME не присвоен при сохранении нового фильма");
        Assert.notNull(film.getDescription(), "DESCRIPTION не присвоен при сохранении нового фильма");
        Assert.notNull(film.getReleaseDate(), "RELEASE_DATE не присвоен при сохранении нового фильма");
        Assert.notNull(film.getDuration(), "DURATION не присвоен при сохранении нового фильма");
        Assert.notNull(film.getMpa().getName(), "MPA_NAME не присвоен при сохранении нового фильма");
        Assert.notNull(film.getGenres().stream().map(Genre::getName), "GENRE_NAME не присвоен при сохранении нового фильма");
    }

    @Test
    @DisplayName("Обновление фильма в БД")
    public void testUpdateFilm() {
        Film updeteFilm = Film.builder()
                .id(filmId)
                .name("Обновленное имя")
                .description("Обновленное описание")
                .releaseDate(LocalDate.of(2025, 1, 1))
                .duration(60)
                .mpa(Mpa.builder()
                        .id(2L)
                        .build())
                .genres(Set.of(Genre.builder()
                        .id((2L))
                        .build()))
                .build();
        Film updatedFilm = filmRepository.update(updeteFilm);

        Assert.isTrue(Objects.equals(updeteFilm.getId(), updatedFilm.getId()), "Вернулся не верный ID при сохранении");
        Assert.isTrue(Objects.equals(updeteFilm.getName(), updatedFilm.getName()), "NAME не обновился");
        Assert.isTrue(Objects.equals(updeteFilm.getDescription(), updatedFilm.getDescription()), "DESCRIPTION не обновился");
        Assert.isTrue(Objects.equals(updeteFilm.getReleaseDate(), updatedFilm.getReleaseDate()), "RELEASE_DATE не обновился");
        Assert.isTrue(Objects.equals(updeteFilm.getDuration(), updatedFilm.getDuration()), "DURATION не обновился");
        Assert.isTrue(Objects.equals(updeteFilm.getMpa().getId(), updatedFilm.getMpa().getId()), "MPA не обновился");
        Assert.notNull(updatedFilm.getMpa().getName(), "MPA_NAME не присвоен при обновлении");
        Assert.isTrue(updatedFilm.getGenres().stream().anyMatch(genre -> genre.getId().equals(2L)), "GENRE не обновился");
        Assert.notNull(updatedFilm.getGenres().stream().map(Genre::getName), "GENRE_NAME не обновился");
    }

    @Test
    @DisplayName("Удаление фильма из БД")
    public void testDeleteFilm() {
        filmRepository.delete(filmId);
        Optional<Film> filmOptional = filmRepository.findById(filmId);

        Assert.isTrue(filmOptional.isEmpty(), "Фильм не удалился");
    }

    @Test
    @DisplayName("Возврат всех фильмов из БД")
    public void testFindAllFilm() {
        List<Film> filmsFromDb = filmRepository.findAll();

        Assert.notEmpty(filmsFromDb, "Список фильмов не вернулся");
    }

    @Test
    @DisplayName("Возврат фильма по ID из БД")
    public void testGetFilm() {
        Optional<Film> filmOptional = filmRepository.findById(filmId);

        Assert.isTrue(filmOptional.isPresent(), "Фильм не вернулся");
    }

    @Test
    @DisplayName("Получение рекомендаций по фильмам из БД")
    public void testGetRecommendations() {
        likeRepository.save(filmId, userId);
        likeRepository.save(filmId, otherUserId);
        likeRepository.save(otherFilmId, otherUserId);
        List<Film> recommendationFilm = filmSearch.findRecommendationsFilms(userId);

        Assert.notEmpty(recommendationFilm, "Список фильмов не вернулся");
        Assert.isTrue(recommendationFilm.size() == 1, "Вернулись оба фильма");
        Assert.isTrue(recommendationFilm.contains(otherFilm), "Порекомендован не тот фильм");
    }
}

