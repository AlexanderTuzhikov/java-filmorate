package ru.yandex.practicum.filmorate.mappers;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    Review mapToReview(NewReviewRequest request);

    ReviewDto mapToReviewDto(Review review);

    static Review updateReviewFields(Review review, UpdateReviewRequest request) {
        return review.toBuilder()
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .build();
    }
}