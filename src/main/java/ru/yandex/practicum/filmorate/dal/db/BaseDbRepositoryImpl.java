package ru.yandex.practicum.filmorate.dal.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class BaseDbRepositoryImpl<T> implements BaseDbRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    @Override
    public long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key != null) {
            log.info("Пользователь успешно сохранен id= {}", key.longValue());
            return key.longValue();
        } else {
            log.error("Ошибка сохранения пользователя");
            throw new InternalServerException("Не удалось сохранить данные — ключ не сгенерирован");
        }

    }

    @Override
    public void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);

        if (rowsUpdated == 0) {
            log.error("Не удалось обновить данные пользователя");
            throw new InternalServerException("Не удалось обновить данные");
        }

        log.info("Данные пользователя успешно обновлены");
    }

    @Override
    public Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    @Override
    public boolean delete(String query, Long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }
}
