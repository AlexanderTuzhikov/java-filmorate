package ru.yandex.practicum.filmorate.exception;

public class NotFoundDirector extends RuntimeException {
    public NotFoundDirector(String message) {
        super(message);
    }

}
