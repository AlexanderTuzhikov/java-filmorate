package ru.yandex.practicum.filmorate.dal.db;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class FriendshipDbRepository extends BaseDbRepositoryImpl<User> {

    public FriendshipDbRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    private static final String INSERT_FRIEND_QUERY = """
            INSERT INTO users_friends (user_id, friend_id, friendship_status_id)
            VALUES (?, ?, ?)
            """;
    private static final String UPDATE_FRIEND_QUERY = """
            UPDATE users_friends SET friendship_status_id = ?
            WHERE user_id = ? AND friend_id = ?
            """;
    private static final String DELETE_FRIEND_QUERY = """
            DELETE FROM users_friends
            WHERE user_id = ? AND friend_id = ?
            """;
    private static final String FIND_ALL_FRIENDS_QUERY = """
            SELECT * FROM users
            WHERE id IN (SELECT uf.friend_id
                        FROM users_friends AS uf
                        LEFT JOIN friendship_status AS fs ON uf.friendship_status_id = fs.id
                        WHERE user_id = ? AND fs.name = 'CONFIRMED')
            """;
    private static final String FIND_COMMON_FRIENDS_QUERY = """
            SELECT * FROM users
            WHERE id IN (SELECT uf1.friend_id
                         FROM users_friends uf1
                         JOIN users_friends uf2 ON uf1.friend_id = uf2.friend_id
                         JOIN friendship_status fs1 ON uf1.friendship_status_id = fs1.id
                         JOIN friendship_status fs2 ON uf2.friendship_status_id = fs2.id
                         WHERE uf1.user_id = ? AND uf2.user_id = ? AND
                               fs1.name= 'CONFIRMED' AND fs2.name = 'CONFIRMED')
            """;

    public boolean save(Long userId, Long friendId) {
        boolean duplicate = isDuplicate(userId, friendId);

        if (duplicate) {
            log.warn("Дружба уже существует, новая запись не может быть добавлена");
            throw new ValidationException("Попытка добавить дубликат дружбы");
        }

        Optional<Long> statusIdConfirmed = getFriendStatusId(FriendshipStatus.CONFIRMED);
        Optional<Long> statusIdNotConfirmed = getFriendStatusId(FriendshipStatus.NOT_CONFIRMED);

        if (statusIdConfirmed.isEmpty() || statusIdNotConfirmed.isEmpty()) {
            throw new IllegalStateException("Не найдены статусы дружбы в таблице friendship_status");
        }

        int rowAddFriend = jdbc.update(INSERT_FRIEND_QUERY, userId, friendId, statusIdConfirmed.get());
        int rowFriendQuery = jdbc.update(INSERT_FRIEND_QUERY, friendId, userId, statusIdNotConfirmed.get());
        return rowAddFriend > 0 && rowFriendQuery > 0;
    }

    public boolean update(Long userId, Long friendId, FriendshipStatus status) {
        Optional<Long> statusId = getFriendStatusId(status);

        if (statusId.isEmpty()) {
            log.warn("Попытка получить несуществующий статус дружбы status= {}", status);
            throw new IllegalStateException("Не найден статус дружбы в таблице friendship_status");
        }

        int rowAddFriend = jdbc.update(UPDATE_FRIEND_QUERY, statusId.get(), userId, friendId);
        log.info("Обновлено строк rowAddFriend= {}", rowAddFriend);
        return rowAddFriend > 0;
    }

    public void delete(Long userId, Long friendId) {
        int rowsDeletedUser = jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
        log.info("Удалено строк rowsDeletedUser= {}", rowsDeletedUser);
        log.info("Дружба удалена id= {} и id= {}", userId, friendId);
    }

    public List<UserDto> findAllFriends(Long userId) {
            return jdbc.query(FIND_ALL_FRIENDS_QUERY, mapper, userId).stream()
                    .map(UserMapper::mapToUserDto)
                    .toList();
    }

    public List<UserDto> findCommonFriends(Long userId, Long otherUserId) {
        return jdbc.query(FIND_COMMON_FRIENDS_QUERY, mapper, userId, otherUserId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    private Optional<Long> getFriendStatusId(@NotNull FriendshipStatus status) {
        String sql = "SELECT id FROM friendship_status WHERE name = ?";

        try {
            Long id = jdbc.queryForObject(sql, Long.class, status.name());
            return Optional.ofNullable(id);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private boolean isDuplicate(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM users_friends WHERE (user_id = ? AND friend_id = ?) " +
                "OR (user_id = ? AND friend_id = ?)";

        Integer rowDuplicate = jdbc.queryForObject(sql, Integer.class, userId, friendId, friendId, userId);

        return rowDuplicate != null && rowDuplicate > 0;
    }
}

