package ru.yandex.practicum.filmorate.dal.db.user;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.db.base.BaseDbRepositoryImpl;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.List;
import java.util.Optional;


@Repository
@Qualifier
@Slf4j
public class UserDbRepository extends BaseDbRepositoryImpl<User> {

    public UserDbRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Language("SQL")
    private static final String INSERT_USER_QUERY = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;
    @Language("SQL")
    private static final String UPDATE_USER_QUERY = """
            UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?
            """;
    @Language("SQL")
    private static final String FIND_ONE_USER_QUERY = """
            SELECT * FROM users WHERE id = ?
            """;
    @Language("SQL")
    private static final String FIND_ALL_USERS_QUERY = """
            SELECT * FROM users
            """;
    @Language("SQL")
    private static final String DELETE_USER_QUERY = """
            DELETE FROM users WHERE id = ?
            """;

    public User save(User user) {
        long id = insert(INSERT_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()));
        Optional<User> savedUser = findOne(FIND_ONE_USER_QUERY, id);

        if (savedUser.isEmpty()) {
            log.error("Ошибка сохранения пользователя userId= {}. Пользователь не найден", user.getId());
            throw new InternalServerException("Ошибка сохранения пользователя. Пользователь не найден");
        }

        return savedUser.get();
    }

    public User update(User user) {
        update(UPDATE_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());
        Optional<User> updateUser = findOne(FIND_ONE_USER_QUERY, user.getId());

        if (updateUser.isEmpty()) {
            log.error("Ошибка обновления пользователя userId= {}. Пользователь не найден", user.getId());
            throw new InternalServerException("Ошибка обновления пользователя. Пользователь не найден");
        }

        return updateUser.get();
    }

    public boolean delete(Long id) {
        return delete(DELETE_USER_QUERY, id);
    }

    public Optional<User> findById(Long id) {
        return findOne(FIND_ONE_USER_QUERY, id);
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_USERS_QUERY);
    }
}
