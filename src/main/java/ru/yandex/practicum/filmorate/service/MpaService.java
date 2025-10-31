package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.MpaDbRepository;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundMpa;
import ru.yandex.practicum.filmorate.mappers.MpaMapper;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MpaService {
    private final MpaDbRepository mpaRepository;

    public List<MpaDto> getAllMpa() {
        return mpaRepository.findAllMpa().stream()
                .map(MpaMapper::mapToMpaDto)
                .toList();
    }

    public MpaDto getMpa(Long mapId) {
        return mpaRepository.findMpa(mapId).map(MpaMapper::mapToMpaDto)
                .orElseThrow(() -> new NotFoundMpa("Mpa с id=" + mapId + " не найден"));
    }
}
