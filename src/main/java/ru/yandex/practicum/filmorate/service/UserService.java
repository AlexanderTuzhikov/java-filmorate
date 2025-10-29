package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.FriendshipDbRepository;
import ru.yandex.practicum.filmorate.dal.db.UserDbRepository;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.mappers.UserMapper.mapToUserDto;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserDbRepository userRepository;
    private final FriendshipDbRepository friendshipRepository;

    public UserDto postUser(NewUserRequest request) {
        User validUser = UserValidator.userValid(request);
        validUser = userRepository.save(validUser);
        return mapToUserDto(validUser);
    }

    public User putUser(@NotNull UpdateUserRequest request) {
        if (request.getId() == null) {
            log.warn("Ошибка валидации: id=null");
            throw new ValidationException("Ошибка валидации: id=null");
        }

        User validUser = UserValidator.userValid(request);
        return userRepository.update(validUser);
    }

    public List<User> getUsers() {
        return userRepository.getAll();
    }

    public User getUser(Long id) {
        return userRepository.get(id);
    }

    public void putFriend(Long id, Long friendId) {
        User user = userRepository.get(id);
        User friend = userRepository.get(friendId);
        user.addFriend(friend, FriendshipStatus.NOT_CONFIRMED);
        friend.addFriend(user, FriendshipStatus.NOT_CONFIRMED);
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = userRepository.get(id);
        User friend = userRepository.get(friendId);
        user.removeFriend(friend);
        friend.removeFriend(user);
    }

    public List<User> getFriends(Long id) {
        User user = userRepository.get(id);

        if (user.getFriends().isEmpty()) {
            return List.of();
        }

        return user.getFriends().keySet().stream()
                .map(userRepository::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = userRepository.get(id);
        User otherUser = userRepository.get(otherId);

        if (user.getFriends().isEmpty()) {
            return List.of();
        }

        Set<Long> userFriends = user.getFriends().keySet();
        userFriends.retainAll(otherUser.getFriends().keySet());

        if (userFriends.isEmpty()) {
            return List.of();
        }

        return userFriends.stream()
                .map(userRepository::get)
                .collect(Collectors.toList());
    }

}
