package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FilmDto> postFilm(@Valid @RequestBody NewFilmRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filmService.postFilm(request));
    }

    @PutMapping
    public ResponseEntity<FilmDto> putFilm(@Valid @RequestBody @NotNull UpdateFilmRequest request) {
        return ResponseEntity.ok().body(filmService.putFilm(request));
    }

    @GetMapping
    public ResponseEntity<List<FilmDto>> getFilms() {
        System.out.println(filmService.getFilms().stream().map(FilmDto::getId).toList());
        return ResponseEntity.ok().body(filmService.getFilms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilm(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(filmService.getFilm(id));
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Void> deleteFilm(@PathVariable("filmId") Long filmId) {
        filmService.deleteFilm(filmId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> putLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.putLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.deleteLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDto>> getFilmsPopular(@RequestParam(name = "count", required = false, defaultValue = "10") int count,
                                                         @RequestParam(name = "genreId", required = false) Long genreId,
                                                         @RequestParam(name = "year", required = false) Integer year) {
        if (genreId != null && year != null) {
            List<FilmDto> popularFilms = filmService.getFilmsPopularByGenreIdAndYear(count, genreId, year);
            return ResponseEntity.ok().body(popularFilms);
        } else if (genreId != null) {
            List<FilmDto> popularFilmsByGenre = filmService.getFilmsPopularByGenreId(count, genreId);
            return ResponseEntity.ok().body(popularFilmsByGenre);
        } else if (year != null) {
            List<FilmDto> popularsFilmsByYear = filmService.getFilmsPopularByYear(count, year);
            return ResponseEntity.ok().body(popularsFilmsByYear);
        } else {
            List<FilmDto> popularFilms = filmService.getFilmsPopular(count);
            return ResponseEntity.ok().body(popularFilms);
        }
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<FilmDto>> getSortedFilms(@PathVariable("directorId") Long directorId,
                                                              @RequestParam(defaultValue = "year") String sortBy) {
        return ResponseEntity.ok().body(filmService.getSortedFilms(directorId, sortBy));
    }

    @GetMapping("/common")
    public ResponseEntity<List<FilmDto>> getCommonFilms(@RequestParam("userId") long userId,
                                                        @RequestParam("friendId") long friendId) {
        List<FilmDto> common = filmService.getCommonFilms(userId, friendId);
        return ResponseEntity.ok().body(common);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilmDto>> searchFilms(@RequestParam String query, @RequestParam String by) {
        return ResponseEntity.ok().body(filmService.searchFilms(query, by));
    }
}