package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.film.FilmDbRepository;
import ru.yandex.practicum.filmorate.dal.db.review.ReviewDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewDbRepository reviewRepository;
    private final UserDbRepository userRepository;
    private final FilmDbRepository filmRepository;
    private final EventService eventService;
    private final ReviewMapper reviewMapper;

    public ReviewDto postReview(NewReviewRequest request) {
        log.info("Получен запрос на добавление отзыва от пользователя id= {}", request.getUserId());
        checkUserExists(request.getUserId());
        checkFilmExists(request.getFilmId());

        Review review = reviewMapper.mapToReview(request);
        Review saved = reviewRepository.save(review);
        eventService.postEvent(saved.getUserId(), saved.getReviewId(), EventType.REVIEW, Operation.ADD);

        return reviewMapper.mapToReviewDto(saved);
    }

    public ReviewDto putReview(UpdateReviewRequest request) {
        log.info("Получен запрос на обновление отзыва id= {}", request.getReviewId());
        Review review = checkReviewExists(request.getReviewId());

        Review updated = ReviewMapper.updateReviewFields(review, request);
        Review updatedFromBd = reviewRepository.update(updated);
        eventService.postEvent(updatedFromBd.getUserId(), updatedFromBd.getReviewId(), EventType.REVIEW, Operation.UPDATE);

        return reviewMapper.mapToReviewDto(updatedFromBd);
    }

    public void deleteReview(Long reviewId) {
        log.info("Получен запрос на обновление удаление id= {}", reviewId);
        Review review = checkReviewExists(reviewId);

        reviewRepository.delete(reviewId);
        eventService.postEvent(review.getUserId(), review.getReviewId(), EventType.REVIEW, Operation.REMOVE);
    }

    public ReviewDto getReview(Long reviewId) {
        log.info("Получен запрос на получение отзыва id= {}", reviewId);
        Review review = checkReviewExists(reviewId);

        return reviewMapper.mapToReviewDto(review);
    }

    public List<ReviewDto> getReviews(Long filmId, Integer count) {
        log.info("Получен запрос на получение списка отзывов count= {}", count);
        int reviewsCount = (count == null) ? 10 : count;
        List<Review> reviews;

        if (filmId != null) {
            checkFilmExists(filmId);
            reviews = reviewRepository.getByFilmId(filmId, reviewsCount);
        } else {
            reviews = reviewRepository.getAll(reviewsCount);
        }

        return reviews.stream()
                .map(reviewMapper::mapToReviewDto)
                .collect(Collectors.toList());
    }

    public void putLike(Long reviewId, Long userId) {
        log.info("Получен запрос на добавление лайка отзыву id= {} от пользователя id= {}", reviewId, userId);
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewRepository.addLike(reviewId, userId);
    }

    public void putDislike(Long reviewId, Long userId) {
        log.info("Получен запрос на добавление дизлайка отзыву id= {} от пользователя id= {}", reviewId, userId);
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewRepository.addDislike(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        log.info("Получен запрос на удаление лайка отзыву id= {} от пользователя id= {}", reviewId, userId);
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewRepository.removeLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        log.info("Получен запрос на удаление дизлайка отзыву id= {} от пользователя id= {}", reviewId, userId);
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewRepository.removeDislike(reviewId, userId);
    }

    private void checkUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private void checkFilmExists(Long filmId) {
        filmRepository.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
    }

    private Review checkReviewExists(Long reviewId) {
        return reviewRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
    }
}