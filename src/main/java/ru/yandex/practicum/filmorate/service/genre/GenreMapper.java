package ru.yandex.practicum.filmorate.service.genre;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.enums.GenreName;
import ru.yandex.practicum.filmorate.model.Genre;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenreMapper {
    public static GenreDto mapToGenreDto(Genre genre) {
        String genreDtoName = GenreName.valueOf(genre.getName()
                .toString())
                .getDtoName();

        return GenreDto.builder()
                .id(genre.getId())
                .name(genreDtoName)
                .build();
    }
}
