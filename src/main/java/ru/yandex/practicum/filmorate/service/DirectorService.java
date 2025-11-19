package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.director.DirectorDbRepository;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

import static ru.yandex.practicum.filmorate.mappers.DirectorMapper.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbRepository directorDbRepository;

    public List<DirectorDto> getAllDirectors() {
        log.info("Получен запрос на получение списка всех directors");
        return directorDbRepository.findAllDirector().stream()
                .map(DirectorMapper::mapToDirectorDto)
                .toList();
    }

    public DirectorDto getDirectorById(Long directorDd) {
        log.info("Получен запрос на получение director id = {}", directorDd);
        return directorDbRepository.findDirector(directorDd).map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> new NotFoundException("Genre с id=" + directorDd + " не найден"));
    }

    public DirectorDto addDirector(NewDirectorRequest request) {
        log.info("Получен запрос на добавление director");
        Director director = mapToDirector(request);
        directorDbRepository.addDirector(director);
        log.info("Режиссер создан. ID= {}", director.getId());
        return mapToDirectorDto(director);
    }

    public DirectorDto updateDirector(UpdateDirectorRequest request) {
        log.info("Получен запрос на обновление director id= {}", request.getId());
        Director director = checkDirectorExists(request.getId());
        Director updatedDirectorLine = updateDirectorFields(director, request);
        Director updatedDirector = directorDbRepository.updateDirector(updatedDirectorLine);
        log.info("Режиссер обновлен. ID= {}", director.getId());
        return mapToDirectorDto(updatedDirector);
    }

    public void removeDirectorById(Long directorId) {
        log.info("Получен запрос на удаление director id= {}", directorId);
        checkDirectorExists(directorId);
        directorDbRepository.removeDirector(directorId);
        log.info("Режиссер с ID= {} - удален", directorId);
    }

    private Director checkDirectorExists(Long directorId) {
        return directorDbRepository.findDirector(directorId).orElseThrow(() -> new NotFoundException("Режиссер с id=" + directorId + " не найден"));
    }
}
