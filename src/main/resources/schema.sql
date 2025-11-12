DROP TABLE IF EXISTS films_likes;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS users_friends;
DROP TABLE IF EXISTS film_directors;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS friendship_status;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS mpa;
DROP TABLE IF EXISTS directors;

CREATE TABLE directors
(
    id  INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(55) UNIQUE
);

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

CREATE TABLE film_directors (
    film_id INT,
    director_id INT REFERENCES directors(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (director_id) REFERENCES directors(id) ON DELETE CASCADE
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

