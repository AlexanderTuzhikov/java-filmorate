package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public ResponseEntity<Collection<DirectorDto>> getAllDirectors() {
        log.info("Получен запрос на получение списка всех directors");
        return ResponseEntity.ok().body(directorService.getAllDirectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDto> getDirectorById(@PathVariable("id") Long directorId) {
        log.info("Получен запрос на получение director id = {}", directorId);
        return ResponseEntity.ok().body(directorService.getDirectorById(directorId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Director> addDirector(@RequestBody Director director) {
        log.info("Получен запрос на добавление director");
        return ResponseEntity.status(HttpStatus.CREATED).body(directorService.addDirector(director));
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@Valid @RequestBody @NotNull Director director) {
        log.info("Получен запрос на обновление director id= {}", director.getId());
        return ResponseEntity.ok().body(directorService.updateDirector(director));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirector(@PathVariable("id") Long directorId) {
        log.info("Получен запрос на удаление director id= {}", directorId);
        directorService.removeDirectorById(directorId);
        return ResponseEntity.ok().build();
    }
}
