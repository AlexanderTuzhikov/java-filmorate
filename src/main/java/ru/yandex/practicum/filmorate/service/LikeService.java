package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.like.LikeDbRepository;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class LikeService {
    private final LikeDbRepository likeRepository;
    private final EventService eventService;

    public void postLike(Long filmId, Long userId) {
        if (likeRepository.findFilmLikes(filmId).contains(userId)) {
            log.info("Повторное добавление лайка фильму id= {}, от пользователя id= {}", filmId, userId);
            eventService.postEvent(userId, filmId, EventType.LIKE, Operation.ADD);
            return;
        }

        likeRepository.save(filmId, userId);
        eventService.postEvent(userId, filmId, EventType.LIKE, Operation.ADD);
    }

    public void deleteLike(Long filmId, Long userId) {
        likeRepository.delete(filmId, userId);
        eventService.postEvent(userId, filmId, EventType.LIKE, Operation.REMOVE);
    }

    public List<Long> getFilmsPopular() {
        return likeRepository.findPopularFilms();
    }
}
