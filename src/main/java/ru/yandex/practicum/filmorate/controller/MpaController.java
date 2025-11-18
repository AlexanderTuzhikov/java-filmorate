package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<List<MpaDto>> getAllMpa() {
        log.info("Получен запрос на получение списка mpa");
        return ResponseEntity.ok().body(mpaService.getAllMpa());
    }

    @GetMapping("/{mapId}")
    public ResponseEntity<MpaDto> getMpa(@PathVariable("mapId") Long mapId) {
        log.info("Получен запрос на получение mpa по id= {}", mapId);
        return ResponseEntity.ok().body(mpaService.getMpa(mapId));
    }
}
