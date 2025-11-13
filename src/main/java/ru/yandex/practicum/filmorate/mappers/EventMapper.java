package ru.yandex.practicum.filmorate.mappers;

import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

public class EventMapper {
    public static EventDto mapEventDto(@NotNull Event event) {
        return EventDto.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .entityId(event.getEntityId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .timestamp(event.getTimestamp())
                .build();
    }
}
