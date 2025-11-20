package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.event.EventDbRepository;
import ru.yandex.practicum.filmorate.dto.event.NewEventRequest;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {
    private final EventDbRepository eventRepository;

    public void postEvent(Long userId, Long entityId,EventType eventType, Operation operation) {
        NewEventRequest newEvent = NewEventRequest.builder()
                .userId(userId)
                .entityId(entityId)
                .eventType(eventType)
                .operation(operation)
                .build();

        eventRepository.save(newEvent);
    }
}
