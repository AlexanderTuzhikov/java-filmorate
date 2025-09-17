package ru.yandex.practicum.filmorate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmorateApplication {
	public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(FilmorateApplication.class);
		SpringApplication.run(FilmorateApplication.class, args);
        log.info("Сервер запущен");
	}
}
