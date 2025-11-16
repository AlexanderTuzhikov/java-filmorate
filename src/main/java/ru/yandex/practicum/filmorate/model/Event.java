package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

@Slf4j
@Data
@Builder(toBuilder = true)
public class Event {
    private final Long id;
    private final Long userId;
    private final Long entityId;
    private final EventType eventType;
    private final Operation operation;
    private Long timestamp;
}
