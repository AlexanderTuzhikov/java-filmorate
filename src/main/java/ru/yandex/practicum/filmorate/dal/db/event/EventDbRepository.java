package ru.yandex.practicum.filmorate.dal.db.event;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.dto.event.NewEventRequest;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class EventDbRepository extends BaseDbRepositoryImpl<Event> {
    public EventDbRepository(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper);
    }

    @Language("SQL")
    private static final String INSERT_EVENT_QUERY = """
            INSERT INTO feed_events (user_id, entity_id, event_type, operation, timestamp)
            VALUES (?, ?, ?, ?, ?)
            """;
    @Language("SQL")
    private static final String FIND_USER_EVENTS_QUERY = """
            SELECT *
            FROM feed_events
            WHERE user_id = ?
            """;
    @Language("SQL")
    private static final String FIND_ONE_EVENTS_QUERY = """
            SELECT *
            FROM feed_events
            WHERE event_id = ?
            """;
    @Language("SQL")
    private static final String FIND_ALL_EVENT_QUERY = """
            SELECT *
            FROM feed_events
            """;



    public Event save(NewEventRequest newEvent) {
        long eventId = insert(INSERT_EVENT_QUERY, newEvent.getUserId(), newEvent.getEntityId(),
                newEvent.getEventType().name(), newEvent.getOperation().name(), newEvent.getTimestamp());
        Optional<Event> savedEvent = findById(eventId);

        if (savedEvent.isEmpty()) {
            log.error("Ошибка сохранения события id= {}. Событие не найдено", eventId);
            throw new InternalServerException("Ошибка после сохранения событие не найдено");
        }

        return savedEvent.get();
    }

    public Optional<Event> findById(Long eventId) {
        return findOne(FIND_ONE_EVENTS_QUERY, eventId);
    }

    public List<Event> findUserEvents(Long userId) {
        return jdbc.query(FIND_USER_EVENTS_QUERY, mapper, userId);
    }

    public List<Event> findAllEvents() {
        return jdbc.query(FIND_ALL_EVENT_QUERY, mapper);
    }
}

