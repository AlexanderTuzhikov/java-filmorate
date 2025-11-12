package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundMpa;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GenreService {
    private final GenreDbRepository genreRepository;

    public List<GenreDto> getAllGenre() {
        return genreRepository.findAllGenre().stream()
                .map(GenreMapper::mapToGenreDto)
                .sorted(Comparator.comparing(GenreDto::getId)) // Сделал вывод по возрастанию как требуют тесты
                .toList();
    }

    public GenreDto getGenre(Long genreId) {
        return genreRepository.findGenre(genreId).map(GenreMapper::mapToGenreDto)
                .orElseThrow(() -> new NotFoundMpa("Genre с id=" + genreId + " не найден"));
    }
}
