package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {
    public static EventDto mapEventDto(@NotNull Event event) {
        return EventDto.builder()
                .eventId(event.getEventId())
                .userId(event.getUserId())
                .entityId(event.getEntityId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .timestamp(event.getTimestamp())
                .build();
    }
}
