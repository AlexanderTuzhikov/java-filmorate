package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectorMapper {
    public static Director mapToDirector(@NotNull NewDirectorRequest request) {
        return Director.builder()
                .name(request.getName())
                .build();
    }

    public static DirectorDto mapToDirectorDto(Director director) {
        return DirectorDto.builder()
                .id(director.getId())
                .name(director.getName())
                .build();
    }

    public static Director updateDirectorFields(@NotNull Director director, @NotNull UpdateDirectorRequest request) {
        Director.DirectorBuilder builder = director.toBuilder();

        if (request.hasName()) {
            builder.name(request.getName());
        }

        return builder.build();
    }
}
