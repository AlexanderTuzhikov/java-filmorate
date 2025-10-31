package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.enums.MpaName;
import ru.yandex.practicum.filmorate.model.Mpa;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MpaMapper {
    public static MpaDto mapToMpaDto(Mpa mpa) {
        return MpaDto.builder()
                .id(mpa.getId())
                .name(toMpaDtoName(mpa.getName()))
                .build();
    }

    private static String toMpaDtoName(MpaName name) {
        return name.toString().replace("_", "-");
    }
}

