package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.director.DirectorDbRepository;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundDirector;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbRepository directorDbRepository;

    public List<DirectorDto> getAllDirectors() {
        return directorDbRepository.findAllDirector().stream()
                .map(DirectorMapper::mapToDirectorDto)
                .toList();
    }

    public DirectorDto getDirectorById(Long directorDd) {
        return directorDbRepository.findDirector(directorDd).map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> new NotFoundException("Genre с id=" + directorDd + " не найден"));
    }

    public Director addDirector(Director director) {
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new ValidationException("Имя режиссера не может быть пустым");
        }
        directorDbRepository.addDirector(director);
        log.info("Режиссер создан. ID= {}", director.getId());
        return director;
    }

    public Director updateDirector(Director director) {
        if (!directorDbRepository.containDirector(director.getId())) {
            throw new NotFoundDirector("Режиссер не найден");
        }

        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new ValidationException("Имя режиссера не может быть пустым");
        }

        directorDbRepository.updateDirector(director);
        log.info("Режиссер обновлен. ID= {}", director.getId());
        return director;
    }

    public void removeDirectorById(Long directorId) {
        if (!directorDbRepository.containDirector(directorId)) {
            throw new NotFoundDirector("Режиссер не найден");
        }
        directorDbRepository.removeDirector(directorId);
        log.info("Режиссер с ID= {} - удален", directorId);
    }
}
