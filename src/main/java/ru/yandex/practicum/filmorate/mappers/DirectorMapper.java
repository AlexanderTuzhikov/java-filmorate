package ru.yandex.practicum.filmorate.mappers;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@Mapper(componentModel = "spring")
public interface DirectorMapper {

    Director mapToDirector(NewDirectorRequest request);

    DirectorDto mapToDirectorDto(Director director);

    static Director updateDirectorFields(@NotNull Director director, @NotNull UpdateDirectorRequest request) {
        Director.DirectorBuilder builder = director.toBuilder();

        if (request.hasName()) {
            builder.name(request.getName());
        }

        return builder.build();
    }
}
