package ru.yandex.practicum.filmorate.exception;

public class NotFoundGenre extends RuntimeException {
    public NotFoundGenre(String message) {
        super(message);
    }
}
