package ru.yandex.practicum.filmorate.dal.db.event;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        long timestamp = resultSet.getLong("timestamp");
        EventType eventType = EventType.valueOf(resultSet.getString("event_type"));
        Operation operation = Operation.valueOf(resultSet.getString("operation"));

        return Event.builder()
                .eventId(resultSet.getLong("id"))
                .userId(resultSet.getLong("user_id"))
                .entityId(resultSet.getLong("entity_id"))
                .eventType(eventType)
                .operation(operation)
                .timestamp(timestamp)
                .build();
    }
}
