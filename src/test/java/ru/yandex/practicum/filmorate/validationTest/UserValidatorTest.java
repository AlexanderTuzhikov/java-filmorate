package ru.yandex.practicum.filmorate.validationTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

public class UserValidatorTest {
    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Test
    @DisplayName("User email empty не проходит валидацию")
    void setEmptyEmail() {
        //Given
        User invalidUser = User.builder()
                .email("")
                .login("TestLogin")
                .name("Test name")
                .birthday(LocalDate.now())
                .build();
        //When
        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        //Then
        assertFalse(violations.isEmpty(), "Валидация с empty email не работает");
    }

    @Test
    @DisplayName("User email invalid не проходит валидацию")
    void setInvalidEmail() {
        //Given
        User invalidUser = User.builder()
                .email("это-неправильный?эмейл@")
                .login("TestLogin")
                .name("Test name")
                .birthday(LocalDate.now())
                .build();
        //When
        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        //Then
        assertFalse(violations.isEmpty(), "Валидация с invalid email не работает");
    }

    @Test
    @DisplayName("User login null не проходит валидацию")
    void setNullLogin() {
        //Given
        User invalidUser = User.builder()
                .email("Test@mail.ru")
                .name("Test name")
                .birthday(LocalDate.now())
                .build();
        //When
        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        //Then
        assertFalse(violations.isEmpty(), "Валидация с null login не работает");
    }

    @Test
    @DisplayName("User login c пробелами не проходит валидацию")
    void setInvalidLogin() {
        //Given
        User invalidUser;
        //When
        invalidUser = User.builder()
                .email("Test@mail.ru")
                .login("Test Login")
                .name("Test name")
                .birthday(LocalDate.now())
                .build();
        //Then
        assertThrows(ValidationException.class, () -> UserValidator.userValid(invalidUser),
                "Валидация login c пробелами не работает");
    }

    @Test
    @DisplayName("User login empty  не проходит валидацию")
    void setEmptyLogin() {
        //Given
        User invalidUser = User.builder()
                .email("Test@mail.ru")
                .login("")
                .name("Test name")
                .birthday(LocalDate.now())
                .build();
        //When
        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        //Then
        assertFalse(violations.isEmpty(), "Валидация с empty login не работает");
    }

    @Test
    @DisplayName("User name empty заменяется на login")
    void setEmptyName() {
        //Given
        User invalidUser = User.builder()
                .email("Test@mail.ru")
                .login("TestLogin")
                .name("")
                .birthday(LocalDate.now())
                .build();
        //When
        User validUser = UserValidator.userValid(invalidUser);
        //Then
        assertEquals(validUser.getLogin(), validUser.getName(), "Валидация с empty name не работает");
    }

    @Test
    @DisplayName("User name null заменяется на login")
    void setNullName() {
        //Given
        User invalidUser = User.builder()
                .email("Test@mail.ru")
                .login("TestLogin")
                .birthday(LocalDate.now())
                .build();
        //When
        User validUser = UserValidator.userValid(invalidUser);
        //Then
        assertEquals(validUser.getLogin(), validUser.getName(), "Валидация с null name не работает");
    }

    @Test
    @DisplayName("User birthday null не проходит валидацию")
    void setNullBirthday() {
        //Given
        User invalidUser = User.builder()
                .email("Test@mail.ru")
                .login("TestLogin")
                .name("Test name")
                .build();
        //When
        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        //Then
        assertFalse(violations.isEmpty(), "Валидация с null birthday не работает");
    }

    @Test
    @DisplayName("User birthday не проходит валидацию")
    void setFutureBirthday() {
        //Given
        User invalidUser = User.builder()
                .email("Test@mail.ru")
                .login("TestLogin")
                .name("Test name")
                .birthday(LocalDate.now().plusDays(1))
                .build();
        //When
        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        //Then
        assertFalse(violations.isEmpty(), "Валидация с future birthday не работает");
        System.out.println(violations);
    }
}
