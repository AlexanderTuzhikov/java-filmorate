package ru.yandex.practicum.filmorate.dal.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;


@Repository
@Qualifier
@Slf4j
public class UserDbRepository extends BaseDbRepositoryImpl<User> {

    public UserDbRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    private static final String INSERT_USER_QUERY = "INSERT INTO users (email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?) returning id";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ?" +
            "WHERE id = ?";
    private static final String FIND_ONE_USER_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String DELETE_USER_QUERY = "DELETE * FROM users WHERE id = ?";

    public User save(User user) {
        long id = insert(INSERT_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        return User.builder()
                .id(id)
                .build();
    }

    public User update(User user) {
        update(UPDATE_USER_QUERY, user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());

        return user;
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
