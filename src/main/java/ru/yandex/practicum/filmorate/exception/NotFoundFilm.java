package ru.yandex.practicum.filmorate.exception;

public class NotFoundFilm extends RuntimeException {
    public NotFoundFilm(String message) {
        super(message);
    }
}
