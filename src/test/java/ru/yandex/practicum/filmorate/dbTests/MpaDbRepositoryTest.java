package ru.yandex.practicum.filmorate.dbTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.dal.db.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbRepository.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbRepositoryTest {
    private final MpaDbRepository mpaRepository;

    @Test
    @DisplayName("Найти Mpa по Id в БД")
    public void testFindMpa() {
        Optional<Mpa> mpa = mpaRepository.findMpa(1L);

        Assert.isTrue(mpa.isPresent(), "Mpa не вернулся");
    }

    @Test
    @DisplayName("Найти список всех Mpa в БД")
    public void testFindAllMpa() {
        List<Mpa> mpa = mpaRepository.findAllMpa();

        Assert.notEmpty(mpa, "Список всех Mpa не вернулся");
    }
}
