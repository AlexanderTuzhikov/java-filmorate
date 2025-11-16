package ru.yandex.practicum.filmorate.dto.event;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

@Data
@Builder(toBuilder = true)
public class NewEventRequest {
    private Long userId;
    private Long entityId;
    private EventType eventType;
    private Operation operation;
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();
}
