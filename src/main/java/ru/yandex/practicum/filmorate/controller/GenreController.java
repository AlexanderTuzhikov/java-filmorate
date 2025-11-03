package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@AllArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<List<GenreDto>> getAllGenre() {
        log.info("Получен запрос на получение списка genre");
        return ResponseEntity.ok().body(genreService.getAllGenre());
    }

    @GetMapping("/{genreId}")
    public ResponseEntity<GenreDto> getGenre(@PathVariable("genreId") Long genreId) {
        log.info("Получен запрос на получение genre по id= {}", genreId);
        return ResponseEntity.ok().body(genreService.getGenre(genreId));
    }
}
