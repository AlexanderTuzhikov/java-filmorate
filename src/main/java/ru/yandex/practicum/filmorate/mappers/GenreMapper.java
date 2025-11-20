package ru.yandex.practicum.filmorate.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.enums.GenreName;
import ru.yandex.practicum.filmorate.model.Genre;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    @Mapping(target = "name", source = "name", qualifiedByName = "mapGenreNameToDtoName")
    GenreDto mapToGenreDto(Genre genre);

    @Named("mapGenreNameToDtoName")
    default String mapGenreNameToDtoName(GenreName genreName) {
        if (genreName == null) return null;
        return genreName.getDtoName();
    }
}
