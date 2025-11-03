package ru.yandex.practicum.filmorate.dbTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import ru.yandex.practicum.filmorate.dal.db.friendship.FriendshipDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserRowMapper;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Objects;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbRepository.class, FriendshipDbRepository.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FriendshipDbRepositoryTest {
    private final UserDbRepository userRepository;
    private final FriendshipDbRepository friendshipRepository;

    private Long userId;
    private Long otherUserId;
    private Long otherUser2Id;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(DataTest.TEST_USER);
        User otherUser = userRepository.save(DataTest.OTHER_TEST_USER);
        User otherUser2 = userRepository.save(DataTest.OTHER_TEST_USER_2);
        userId = user.getId();
        otherUserId = otherUser.getId();
        otherUser2Id = otherUser2.getId();
    }

    @Test
    @DisplayName("Сохранение дружбы в БД")
    public void testSaveFriendship() {
        friendshipRepository.save(userId, otherUserId);
        List<User> userFriends = friendshipRepository.findAllFriends(userId);
        List<User> otherUserFriends = friendshipRepository.findAllFriends(userId);

        Assert.isTrue(
                userFriends.stream()
                        .anyMatch(friend -> Objects.equals(friend.getId(), otherUserId)),
                "Связь дружбы user - otherUser не сохранилась"
        );
        Assert.isTrue(
                otherUserFriends.stream()
                        .noneMatch(friend -> Objects.equals(friend.getId(), userId)),
                "Сохранилась связь дружбы otherUser - user status=NOT_CONFIRMED"
        );
    }

    @Test
    @DisplayName("Обновление дружбы в БД")
    public void testUpdateFriendship() {
        friendshipRepository.save(userId, otherUserId);
        friendshipRepository.update(otherUserId, userId, FriendshipStatus.CONFIRMED);
        List<User> otherUserFriends = friendshipRepository.findAllFriends(otherUserId);

        Assert.isTrue(
                otherUserFriends.stream()
                        .anyMatch(friend -> Objects.equals(friend.getId(), userId)),
                "Связь дружбы otherUser - user не обновилась"
        );
    }

    @Test
    @DisplayName("Обновление дружбы в БД")
    public void testDeleteFriendship() {
        friendshipRepository.save(userId, otherUserId);
        friendshipRepository.update(otherUserId, userId, FriendshipStatus.CONFIRMED);
        friendshipRepository.delete(userId, otherUserId);
        List<User> userFriends = friendshipRepository.findAllFriends(otherUserId);
        List<User> otherUserFriends = friendshipRepository.findAllFriends(otherUserId);

        Assert.isTrue(
                userFriends.stream()
                        .noneMatch(friend -> Objects.equals(friend.getId(), otherUserId)),
                "Связь дружбы user - otherUser не удалилась"
        );

        Assert.isTrue(
                otherUserFriends.stream()
                        .anyMatch(friend -> Objects.equals(friend.getId(), userId)),
                "Связь дружбы otherUser - user удалилась"
        );
    }

    @Test
    @DisplayName("Найти всех друзей в БД")
    public void testFindAllFriends() {
        friendshipRepository.save(userId, otherUserId);
        friendshipRepository.save(userId, otherUser2Id);
        List<User> userFriends = friendshipRepository.findAllFriends(userId);

        Assert.isTrue(userFriends.size() == 2, "Список всех друзей не вернулся"
        );
    }

    @Test
    @DisplayName("Найти общих друзей в БД")
    public void testFindCommonFriends() {
        friendshipRepository.save(userId, otherUser2Id);
        friendshipRepository.save(otherUserId, otherUser2Id);
        List<User> commonFriends = friendshipRepository.findCommonFriends(userId, otherUserId);

        Assert.isTrue(
                commonFriends.stream()
                        .anyMatch(friend -> Objects.equals(friend.getId(), otherUser2Id)),
                "Общий друг не найден"
        );
    }
}




