package ru.yandex.practicum.filmorate.mappers;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventDto mapEventDto(Event event);
}
