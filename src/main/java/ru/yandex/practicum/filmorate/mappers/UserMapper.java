package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static User mapToUser(NewUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .login(request.getLogin())
                .name(request.getName())
                .birthday(request.getBirthday())
                .build();
    }

    public static UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }

    public static User updateImageFields(User user, UpdateUserRequest request) {
        if (request.hasEmail()) {
            user.toBuilder()
                    .email(request.getEmail())
                    .build();
        }

        if (request.hasLogin()) {
            user.toBuilder()
                    .login(request.getLogin())
                    .build();
        }

        if (request.hasName()) {
            user.toBuilder()
                    .name(request.getName())
                    .build();
        }

        if (request.hasBirthday()) {
            user.toBuilder()
                    .birthday(request.getBirthday())
                    .build();
        }

        return user;
    }
}
