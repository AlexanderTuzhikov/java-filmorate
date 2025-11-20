package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @NotNull(message = "ID отзыва не может быть пустым")
    private Long reviewId;
    @NotBlank(message = "Содержимое отзыва не может быть пустым")
    private String content;
    @NotNull(message = "Тип отзыва (положительный/отрицательный) не может быть пустым")
    private Boolean isPositive;
}