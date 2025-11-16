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
import ru.yandex.practicum.filmorate.dal.db.film.FilmDbRepository;
import ru.yandex.practicum.filmorate.dal.db.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.db.like.LikeDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@Import({LikeDbRepository.class, FilmDbRepository.class, GenreDbRepository.class, MpaDbRepository.class,
        FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class,
        UserDbRepository.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LikeDbRepositoryTest {
    private final LikeDbRepository likeRepository;
    private final FilmDbRepository filmRepository;
    private final UserDbRepository userRepository;

    private Long filmId;
    private Long otherFilmId;
    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        Film film = filmRepository.save(DataTest.TEST_FILM);
        Film otherFilm = filmRepository.save(DataTest.OTHER_TEST_FILM);
        filmId = film.getId();
        otherFilmId = otherFilm.getId();
        User user = userRepository.save(DataTest.TEST_USER);
        User otherUser = userRepository.save(DataTest.OTHER_TEST_USER);
        userId = user.getId();
        otherUserId = otherUser.getId();
    }

    @Test
    @DisplayName("Сохранение лайка в БД")
    public void testSaveLike() {
        likeRepository.save(filmId, userId);
        List<Long> likes = likeRepository.findFilmLikes(filmId);

        Assert.notEmpty(likes, "Лайк не сохранился");
    }

    @Test
    @DisplayName("Удаление лайка в БД")
    public void testDeleteLike() {
        likeRepository.save(filmId, userId);
        likeRepository.delete(filmId, userId);

        List<Long> likes = likeRepository.findFilmLikes(filmId);

        Assert.isTrue(likes.isEmpty(), "Лайк не удалился");
    }

    @Test
    @DisplayName("Получение списка популярных фильмов в БД")
    public void testFindPopularFilms() {
        likeRepository.save(filmId, userId);
        likeRepository.save(filmId, otherUserId);
        likeRepository.save(otherFilmId, userId);

        List<Long> popularFilms = likeRepository.findPopularFilms().stream()
                .limit(1)
                .toList();

        Assert.isTrue(popularFilms.size() == 1, "Count не соблюдается");
    }
}

