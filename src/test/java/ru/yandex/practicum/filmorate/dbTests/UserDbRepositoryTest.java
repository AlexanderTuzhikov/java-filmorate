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
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbRepository.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbRepositoryTest {
    private final UserDbRepository userRepository;

    private User user;
    private Long userId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(DataTest.TEST_USER);
        userId = user.getId();
    }

    @Test
    @DisplayName("Сохранение нового пользователя в БД")
    public void testSaveUser() {
        Assert.notNull(user, "Вернулся NULL при сохранении");
        Assert.notNull(userId, "ID не присвоен при сохранении нового пользователя");
        Assert.notNull(user.getEmail(), "EMAIL не присвоен при сохранении нового пользователя");
        Assert.notNull(user.getLogin(), "LOGIN не присвоен при сохранении нового пользователя");
        Assert.notNull(user.getName(), "NAME не присвоен при сохранении нового пользователя");
        Assert.notNull(user.getBirthday(), "BIRTHDAY не присвоен при сохранении нового пользователя");
    }

    @Test
    @DisplayName("Обновление фильма в БД")
    public void testUpdateUser() {
        User updeteUser = User.builder()
                .id(userId)
                .email("update@email,ru")
                .login("Update login")
                .name("Update name")
                .birthday(LocalDate.of(2025, 1, 1))
                .build();
        User updatedUser = userRepository.update(updeteUser);

        Assert.isTrue(Objects.equals(updeteUser.getId(), updatedUser.getId()), "Вернулся не верный ID при сохранении");
        Assert.isTrue(Objects.equals(updeteUser.getEmail(), updatedUser.getEmail()), "EMAIL не обновился");
        Assert.isTrue(Objects.equals(updeteUser.getLogin(), updatedUser.getLogin()), "LOGIN не обновился");
        Assert.isTrue(Objects.equals(updeteUser.getName(), updatedUser.getName()), "NAME не обновился");
        Assert.isTrue(Objects.equals(updeteUser.getBirthday(), updatedUser.getBirthday()), "BIRTHDAY не обновился");
    }

    @Test
    @DisplayName("Удаление фильма из БД")
    public void testDeleteUser() {
        userRepository.delete(userId);
        Optional<User> filmOptional = userRepository.findById(userId);

        Assert.isTrue(filmOptional.isEmpty(), "Пользователь не удалился");
    }

    @Test
    @DisplayName("Возврат всех пользователей из БД")
    public void testFindAllUser() {
        List<User> usersFromDb = userRepository.findAll();

        Assert.notEmpty(usersFromDb, "Список пользователей не вернулся");
    }

    @Test
    @DisplayName("Возврат пользователя по ID из БД")
    public void testGetFilm() {
        Optional<User> userOptional = userRepository.findById(userId);

        Assert.isTrue(userOptional.isPresent(), "Пользователь не вернулся");
    }
}
