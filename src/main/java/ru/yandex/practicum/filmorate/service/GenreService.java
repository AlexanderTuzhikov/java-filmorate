package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class GenreService {
    private final GenreDbRepository genreRepository;
    private final GenreMapper genreMapper;

    public List<GenreDto> getAllGenre() {
        log.info("Получен запрос на получение списка genre");
        return genreRepository.findAllGenre().stream()
                .map(genreMapper::mapToGenreDto)
                .sorted(Comparator.comparing(GenreDto::getId))
                .toList();
    }

    public GenreDto getGenre(Long genreId) {
        log.info("Получен запрос на получение genre по id= {}", genreId);
        Genre genre = checkGenreExists(genreId);
        return genreMapper.mapToGenreDto(genre);
    }

    private Genre checkGenreExists(Long genreId) {
        return genreRepository.findGenre(genreId)
                .orElseThrow(() -> new NotFoundException("Genre с id=" + genreId + " не найден"));
    }
}
