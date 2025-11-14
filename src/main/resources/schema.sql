DROP TABLE IF EXISTS review_likes;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS films_likes;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS users_friends;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS friendship_status;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS mpa;

CREATE TABLE mpa
(
    id  INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) UNIQUE
);

CREATE TABLE genres
(
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE
);

CREATE TABLE films
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(40)  NOT NULL,
    description  VARCHAR(200) NOT NULL,
    release_date DATE         NOT NULL,
    duration     INT          NOT NULL CHECK (duration > 0),
    mpa_id      INT,
    FOREIGN KEY (mpa_id) REFERENCES mpa (id)
);

CREATE TABLE users
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    email    VARCHAR(50) NOT NULL UNIQUE,
    login    VARCHAR(50) NOT NULL,
    name     VARCHAR(50),
    birthday DATE        NOT NULL
);

CREATE TABLE friendship_status
(
    id     INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) UNIQUE
);

CREATE TABLE users_friends
(
    user_id              INT,
    friend_id            INT,
    friendship_status_id INT,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (friendship_status_id) REFERENCES friendship_status (id)
);

CREATE TABLE film_genres
(
    film_id  INT,
    genre_id INT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE CASCADE
);

CREATE TABLE films_likes
(
    film_id INT,
    user_id INT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE reviews (
                         review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         content TEXT NOT NULL,
                         is_positive BOOLEAN NOT NULL,
                         user_id BIGINT NOT NULL,
                         film_id BIGINT NOT NULL,
                         useful INT DEFAULT 0,
                         FOREIGN KEY (user_id) REFERENCES users(id),
                         FOREIGN KEY (film_id) REFERENCES films(id)
);

CREATE TABLE review_likes (
                              review_id BIGINT,
                              user_id BIGINT,
                              rating INT CHECK (rating IN (-1, 1)),
                              PRIMARY KEY (review_id, user_id),
                              FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
                              FOREIGN KEY (user_id) REFERENCES users(id)
);