package ru.yandex.practicum.filmorate.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.enums.MpaName;
import ru.yandex.practicum.filmorate.model.Mpa;

@Mapper(componentModel = "spring")
public interface MpaMapper {
    @Mapping(target = "name", source = "name", qualifiedByName = "toMpaDtoName")
    MpaDto mapToMpaDto(Mpa mpa);

    @Named("toMpaDtoName")
    default String toMpaDtoName(MpaName name) {
        if (name == null) return null;
        return name.toString().replace("_", "-");
    }
}

