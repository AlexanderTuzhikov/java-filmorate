package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.db.event.EventDbRepository;
import ru.yandex.practicum.filmorate.dal.db.film.FilmDbRepository;
import ru.yandex.practicum.filmorate.dal.db.review.ReviewDbRepository;
import ru.yandex.practicum.filmorate.dal.db.user.UserDbRepository;
import ru.yandex.practicum.filmorate.dto.event.NewEventRequest;
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

@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewDbRepository reviewRepository;
    private final UserDbRepository userRepository;
    private final FilmDbRepository filmRepository;
    private final EventDbRepository eventRepository;

    public ReviewDto postReview(NewReviewRequest request) {
        checkUserExists(request.getUserId());
        checkFilmExists(request.getFilmId());

        Review review = ReviewMapper.mapToReview(request);
        Review saved = reviewRepository.save(review);
        saveReviewEvent(saved.getUserId(), saved.getReviewId(), Operation.ADD);
        return ReviewMapper.mapToReviewDto(saved);
    }

    public ReviewDto putReview(UpdateReviewRequest request) {
        Review review = reviewRepository.getById(request.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + request.getReviewId() + " не найден"));
        Review updated = ReviewMapper.updateReviewFields(review, request);
        Review updatedFromBd = reviewRepository.update(updated);
        saveReviewEvent(updatedFromBd.getUserId(), updatedFromBd.getReviewId(), Operation.UPDATE);
        return ReviewMapper.mapToReviewDto(updatedFromBd);
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
        reviewRepository.delete(reviewId);
        saveReviewEvent(review.getUserId(), review.getReviewId(), Operation.REMOVE);
    }

    public ReviewDto getReview(Long reviewId) {
        Review review = reviewRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
        return ReviewMapper.mapToReviewDto(review);
    }

    public List<ReviewDto> getReviews(Long filmId, Integer count) {
        int c = (count == null) ? 10 : count;
        List<Review> reviews;

        if (filmId != null) {
            filmRepository.findById(filmId)
                    .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
            reviews = reviewRepository.getByFilmId(filmId, c);
        } else {
            reviews = reviewRepository.getAll(c);
        }

        return reviews.stream()
                .map(ReviewMapper::mapToReviewDto)
                .collect(Collectors.toList());
    }

    public void putLike(Long reviewId, Long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);
        saveReviewEvent(userId, reviewId, Operation.UPDATE);
        reviewRepository.addLike(reviewId, userId);
    }

    public void putDislike(Long reviewId, Long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewRepository.addDislike(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);
        saveReviewEvent(userId, reviewId, Operation.REMOVE);
        reviewRepository.removeLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewRepository.removeDislike(reviewId, userId);
    }

    private void saveReviewEvent(Long userId, Long entityId, Operation operation) {
        NewEventRequest newEvent = NewEventRequest.builder()
                .userId(userId)
                .entityId(entityId)
                .eventType(EventType.REVIEW)
                .operation(operation)
                .build();

        eventRepository.save(newEvent);
    }

    private void checkUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private void checkFilmExists(Long filmId) {
        filmRepository.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
    }

    private void checkReviewExists(Long reviewId) {
        reviewRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
    }
}