package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewReviewRequest {
    @NotBlank(message = "Содержимое отзыва не может быть пустым")
    private String content;
    @NotNull(message = "Тип отзыва (положительный/отрицательный) не может быть пустым")
    private Boolean isPositive;
    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;
    @NotNull(message = "ID фильма не может быть пустым")
    private Long filmId;
}