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
        return ResponseEntity.ok().body(directorService.getAllDirectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDto> getDirectorById(@PathVariable("id") Long directorId) {
        return ResponseEntity.ok().body(directorService.getDirectorById(directorId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Director> addDirector(@RequestBody Director director) {
        return ResponseEntity.status(HttpStatus.CREATED).body(directorService.addDirector(director));
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@Valid @RequestBody @NotNull Director director) {
        return ResponseEntity.ok().body(directorService.updateDirector(director));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirector(@PathVariable("id") Long directorId) {
        directorService.removeDirectorById(directorId);
        return ResponseEntity.ok().build();
    }

}
