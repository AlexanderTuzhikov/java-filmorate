package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;


    @PostMapping
    public ResponseEntity<ReviewDto> postReview(@Valid @RequestBody NewReviewRequest request) {
        log.info("Получен запрос на добавление отзыва от пользователя id= {}", request.getUserId());
        ReviewDto review = reviewService.postReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping
    public ResponseEntity<ReviewDto> putReview(@Valid @RequestBody UpdateReviewRequest request) {
        log.info("Получен запрос на обновление отзыва id= {}", request.getReviewId());
        ReviewDto review = reviewService.putReview(request);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.info("Получен запрос на обновление удаление id= {}", id);
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable Long id) {
        log.info("Получен запрос на получение отзыва id= {}", id);
        ReviewDto review = reviewService.getReview(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getReviews(@RequestParam(required = false) Long filmId,
                                                      @RequestParam(defaultValue = "10") Integer count) {
        log.info("Получен запрос на получение списка отзывов count= {}", count);
        List<ReviewDto> reviews = reviewService.getReviews(filmId, count);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка отзыву id= {} от пользователя id= {}", id, userId);
        reviewService.putLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление дизлайка отзыву id= {} от пользователя id= {}", id, userId);
        reviewService.putDislike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка отзыву id= {} от пользователя id= {}", id, userId);
        reviewService.deleteLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление дизлайка отзыву id= {} от пользователя id= {}", id, userId);
        reviewService.deleteDislike(id, userId);
        return ResponseEntity.ok().build();
    }
}