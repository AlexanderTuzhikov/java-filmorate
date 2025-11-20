package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.friendship.FriendshipDbRepository;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FriendshipService {
    private final FriendshipDbRepository friendshipRepository;
    private final EventService eventService;
    private final UserMapper userMapper;

    public void postFriendship(Long userId, Long friendId) {
        boolean status = friendshipRepository.save(userId, friendId);

        if (!status) {
            log.error("Ошибка сервера при обработки запроса добавления в дружбу пользователей userId= {}," +
                    " friendId= {}", userId, friendId);
            throw new InternalServerException("Ошибка сервера при добавлении в друзья");
        }

        log.info("Пользователь id= {} добавил друга id= {} статус дружбы=CONFIRMED. " +
                "Пользователь id= {} получил запрос на добавление в друзья от id= {} статус дружбы=NOT_CONFIRMED.", userId, friendId, friendId, userId);

        eventService.postEvent(userId, friendId,EventType.FRIEND, Operation.ADD);
    }

    public void deleteFriendship(Long userId, Long friendId) {
        friendshipRepository.delete(userId, friendId);
        eventService.postEvent(userId, friendId,EventType.FRIEND, Operation.REMOVE);
    }

    public List<UserDto> getFriends(Long userId) {
        return friendshipRepository.findAllFriends(userId).stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(Long userId, Long friendId) {
        return friendshipRepository.findCommonFriends(userId, friendId).stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }
}
