package ru.yandex.practicum.filmorate.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.*;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({NotFoundUser.class, NotFoundFilm.class, NotFoundMpa.class, NotFoundGenre.class,
            NotFoundDirector.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final @NotNull RuntimeException exception) {
        log.error("Запрос на несуществующий ресурс. Error: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage(), "Ресурс не найден");
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final @NotNull ValidationException exception) {
        log.error("Ошибка валидации данных. Error: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage(), "Ошибка валидации данных");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<List<String>> handleValidationExceptions(@NotNull MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage()
                        + " Получены данные: " + error.getRejectedValue())
                .toList();
        log.warn("Ошибка валидации: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final @NotNull Throwable exception) {
        log.error("Внутренняя ошибка сервера. Error: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage(), "Внутренняя ошибка сервера");
    }
}
