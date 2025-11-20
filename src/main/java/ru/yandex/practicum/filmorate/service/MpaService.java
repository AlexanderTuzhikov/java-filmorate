package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MpaService {
    private final MpaDbRepository mpaRepository;
    private final MpaMapper mpaMapper;


    public List<MpaDto> getAllMpa() {
        log.info("Получен запрос на получение списка mpa");
        return mpaRepository.findAllMpa().stream()
                .map(mpaMapper::mapToMpaDto)
                .sorted(Comparator.comparing(MpaDto::getId))
                .toList();
    }

    public MpaDto getMpa(Long mapId) {
        log.info("Получен запрос на получение mpa по id= {}", mapId);
        Mpa mpa = checkMpaExists(mapId);
        return mpaMapper.mapToMpaDto(mpa);
    }

    private Mpa checkMpaExists(Long mapId) {
        return mpaRepository.findMpa(mapId)
                .orElseThrow(() -> new NotFoundException("Mpa с id=" + mapId + " не найден"));
    }
}
