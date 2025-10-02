package ru.yandex.practicum.filmorate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.zalando.logbook.Logbook;

@SpringBootApplication
public class FilmorateApplication {
	public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(FilmorateApplication.class);
        Logbook logbook = Logbook.create();
		SpringApplication.run(FilmorateApplication.class, args);
        log.info("Сервер запущен");
	}
}
