package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewMapper {

    public static Review mapToReview(NewReviewRequest request) {
        return Review.builder()
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .userId(request.getUserId())
                .filmId(request.getFilmId())
                .useful(0)
                .build();
    }

    public static ReviewDto mapToReviewDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
    }

    public static Review updateReviewFields(Review review, UpdateReviewRequest request) {
        return review.toBuilder()
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .build();
    }
}