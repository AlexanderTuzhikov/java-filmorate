package ru.yandex.practicum.filmorate.dal.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class GenreDbRepository extends BaseDbRepositoryImpl<Genre> {

    public GenreDbRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_GENRE_QUERY = """
            SELECT *
            FROM genres
            WHERE id = ?
            """;

    private static final String FIND_ALL_GENRE_QUERY = """
            SELECT *
            FROM genres
            """;

    public Optional<Genre> findGenre(Long genreId) {
        return findOne(FIND_GENRE_QUERY, genreId);
    }

    public List<Genre> findAllGenre() {
        return findMany(FIND_ALL_GENRE_QUERY);
    }


}
