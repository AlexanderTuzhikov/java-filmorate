package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.event.EventDbRepository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FeedService {
    private final EventDbRepository eventRepository;

    public List<Event> getUserFeed(Long userId) {
        return eventRepository.findUserEvents(userId);
    }
}
