package ru.yandex.practicum.filmorate.dbTests;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

public class DataTest {
    public static Film TEST_FILM = Film.builder()
            .name("Тестовый фильм")
            .description("Описание фильма")
            .releaseDate(LocalDate.now())
            .duration(120)
            .mpa(Mpa.builder()
                    .id(1L)
                    .build())
            .genres(Set.of(Genre.builder().id(1L).build()))
            .build();

    public static Film OTHER_TEST_FILM = Film.builder()
            .name("Тестовый фильм 2")
            .description("Описание фильма 2")
            .releaseDate(LocalDate.now())
            .duration(60)
            .mpa(Mpa.builder()
                    .id(2L)
                    .build())
            .genres(Set.of(Genre.builder().id(2L).build()))
            .build();

    public static User TEST_USER= User.builder()
            .email("test@mail.ru")
            .login("Тестовый логин")
            .name("Тестовое имя")
            .birthday(LocalDate.now())
            .build();

    public static User OTHER_TEST_USER = User.builder()
            .email("other_test@mail.ru")
            .login("Тестовый логин 2")
            .name("Тестовое имя 2")
            .birthday(LocalDate.now())
            .build();

    public static User OTHER_TEST_USER_2 = User.builder()
            .email("other_test2@mail.ru")
            .login("Тестовый логин 3")
            .name("Тестовое имя 3")
            .birthday(LocalDate.now())
            .build();
}

