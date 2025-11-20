package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

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
    public ResponseEntity<DirectorDto> addDirector(@Valid @RequestBody NewDirectorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(directorService.addDirector(request));
    }

    @PutMapping
    public ResponseEntity<DirectorDto> updateDirector(@Valid @RequestBody @NotNull UpdateDirectorRequest request) {
        return ResponseEntity.ok().body(directorService.updateDirector(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirector(@PathVariable("id") Long directorId) {
        directorService.removeDirectorById(directorId);
        return ResponseEntity.ok().build();
    }
}
