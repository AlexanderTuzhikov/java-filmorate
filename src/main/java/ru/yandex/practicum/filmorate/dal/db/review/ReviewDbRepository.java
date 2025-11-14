package ru.yandex.practicum.filmorate.dal.db.review;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class ReviewDbRepository {
    private final JdbcTemplate jdbc;
    private final ReviewRowMapper rowMapper;

    private static final String INSERT_REVIEW = """
            INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_REVIEW = """
            UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?
            """;

    private static final String DELETE_REVIEW = "DELETE FROM reviews WHERE review_id = ?";
    private static final String FIND_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String FIND_REVIEWS_BY_FILM = """
            SELECT * FROM reviews WHERE film_id = ? 
            ORDER BY useful DESC LIMIT ?
            """;
    private static final String FIND_ALL_REVIEWS = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";

    private static final String CHECK_USER_REACTION = """
            SELECT rating FROM review_likes WHERE review_id = ? AND user_id = ?
            """;
    private static final String INSERT_REACTION = """
            INSERT INTO review_likes (review_id, user_id, rating) VALUES (?, ?, ?)
            """;
    private static final String UPDATE_REACTION = """
            UPDATE review_likes SET rating = ? WHERE review_id = ? AND user_id = ?
            """;
    private static final String DELETE_REACTION = """
            DELETE FROM review_likes WHERE review_id = ? AND user_id = ?
            """;
    private static final String CALCULATE_USEFUL = """
            SELECT COALESCE(SUM(rating), 0) 
            FROM review_likes 
            WHERE review_id = ?
            """;
    private static final String UPDATE_USEFUL = "UPDATE reviews SET useful = ? WHERE review_id = ?";

    public Review save(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return getById(id).orElseThrow();
    }

    public Review update(Review review) {
        jdbc.update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        updateUseful(review.getReviewId());
        return getById(review.getReviewId()).orElseThrow();
    }

    public void delete(Long reviewId) {
        jdbc.update("DELETE FROM review_likes WHERE review_id = ?", reviewId);
        jdbc.update(DELETE_REVIEW, reviewId);
    }

    public Optional<Review> getById(Long reviewId) {
        List<Review> reviews = jdbc.query(FIND_REVIEW_BY_ID, rowMapper, reviewId);
        return reviews.stream().findFirst();
    }

    public List<Review> getByFilmId(Long filmId, int count) {
        return jdbc.query(FIND_REVIEWS_BY_FILM, rowMapper, filmId, count);
    }

    public List<Review> getAll(int count) {
        return jdbc.query(FIND_ALL_REVIEWS, rowMapper, count);
    }

    public void addLike(Long reviewId, Long userId) {
        try {
            Integer existingRating = jdbc.queryForObject(CHECK_USER_REACTION, Integer.class, reviewId, userId);

            if (existingRating == null) {
                jdbc.update(INSERT_REACTION, reviewId, userId, 1);
            } else if (existingRating == -1) {
                jdbc.update(UPDATE_REACTION, 1, reviewId, userId);
            } else if (existingRating == 1) {
                return;
            }

            updateUseful(reviewId);
        } catch (Exception e) {
            jdbc.update(INSERT_REACTION, reviewId, userId, 1);
            updateUseful(reviewId);
        }
    }

    public void addDislike(Long reviewId, Long userId) {
        try {
            Integer existingRating = jdbc.queryForObject(CHECK_USER_REACTION, Integer.class, reviewId, userId);

            if (existingRating == null) {
                jdbc.update(INSERT_REACTION, reviewId, userId, -1);
            } else if (existingRating == 1) {
                jdbc.update(UPDATE_REACTION, -1, reviewId, userId);
            } else if (existingRating == -1) {
                return;
            }

            updateUseful(reviewId);
        } catch (Exception e) {
            jdbc.update(INSERT_REACTION, reviewId, userId, -1);
            updateUseful(reviewId);
        }
    }

    public void removeLike(Long reviewId, Long userId) {
        try {
            Integer existingRating = jdbc.queryForObject(CHECK_USER_REACTION, Integer.class, reviewId, userId);

            if (existingRating != null && existingRating == 1) {
                jdbc.update(DELETE_REACTION, reviewId, userId);
                updateUseful(reviewId);
            }
        } catch (Exception e) {
        }
    }

    public void removeDislike(Long reviewId, Long userId) {
        try {
            Integer existingRating = jdbc.queryForObject(CHECK_USER_REACTION, Integer.class, reviewId, userId);

            if (existingRating != null && existingRating == -1) {
                jdbc.update(DELETE_REACTION, reviewId, userId);
                updateUseful(reviewId);
            }
        } catch (Exception e) {
        }
    }

    private void updateUseful(Long reviewId) {
        try {
            Integer useful = jdbc.queryForObject(CALCULATE_USEFUL, Integer.class, reviewId);
            if (useful != null) {
                jdbc.update(UPDATE_USEFUL, useful, reviewId);
            }
        } catch (Exception e) {
            jdbc.update(UPDATE_USEFUL, 0, reviewId);
        }
    }
}