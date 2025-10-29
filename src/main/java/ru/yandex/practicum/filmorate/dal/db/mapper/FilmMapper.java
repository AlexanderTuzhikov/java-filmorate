package ru.yandex.practicum.filmorate.dal.db.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.Mpa;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class FilmMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Timestamp releaseDate = resultSet.getTimestamp("releaseDate");

        return Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(releaseDate.toLocalDateTime().toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(Mpa.valueOf(resultSet.getString("mpa")))
                .build();
    }
}
