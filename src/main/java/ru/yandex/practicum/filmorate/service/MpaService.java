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

import static ru.yandex.practicum.filmorate.mappers.MpaMapper.mapToMpaDto;

@Slf4j
@Service
@AllArgsConstructor
public class MpaService {
    private final MpaDbRepository mpaRepository;

    public List<MpaDto> getAllMpa() {
        return mpaRepository.findAllMpa().stream()
                .map(MpaMapper::mapToMpaDto)
                .sorted(Comparator.comparing(MpaDto::getId))
                .toList();
    }

    public MpaDto getMpa(Long mapId) {
        Mpa mpa = checkMpaExists(mapId);
        return mapToMpaDto(mpa);
    }

    private Mpa checkMpaExists(Long mapId) {
        return mpaRepository.findMpa(mapId)
                .orElseThrow(() -> new NotFoundException("Mpa с id=" + mapId + " не найден"));
    }
}
