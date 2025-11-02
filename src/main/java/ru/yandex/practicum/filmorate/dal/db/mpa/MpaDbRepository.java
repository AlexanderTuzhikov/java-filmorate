package ru.yandex.practicum.filmorate.dal.db.mpa;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class MpaDbRepository extends BaseDbRepositoryImpl<Mpa> {

    public MpaDbRepository(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    @Language("SQL")
    private static final String FIND_MPA_QUERY = """
            SELECT *
            FROM mpa
            WHERE id = ?
            """;
    @Language("SQL")
    private static final String FIND_ALL_MPA_QUERY = """
            SELECT *
            FROM mpa
            """;

    public Optional<Mpa> findMpa(Long mpaId) {
        return findOne(FIND_MPA_QUERY, mpaId);
    }

    public List<Mpa> findAllMpa() {
        return findMany(FIND_ALL_MPA_QUERY);
    }
}
