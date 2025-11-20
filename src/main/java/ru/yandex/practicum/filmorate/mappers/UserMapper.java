package ru.yandex.practicum.filmorate.mappers;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User mapToUser(NewUserRequest request);

    UserDto mapToUserDto(User user);

    static User updateUserFields(User user, UpdateUserRequest request) {
        User.UserBuilder builder = user.toBuilder();

        if (request.hasEmail()) {
            builder.email(request.getEmail());
        }

        if (request.hasLogin()) {
            builder.login(request.getLogin());
        }

        if (request.hasName()) {
            builder.name(request.getName());
        }

        if (request.hasBirthday()) {
            builder.birthday(request.getBirthday());
        }

        return builder.build();
    }
}
