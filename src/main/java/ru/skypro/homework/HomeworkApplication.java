package ru.skypro.homework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Главный класс приложения Homework.
 * <p>
 * Точка входа в Spring Boot‑приложение. Включает:
 * - автоматическую конфигурацию компонентов (@SpringBootApplication);
 * - поддержку запланированных задач (@EnableScheduling).
 * </p>
 */
@SpringBootApplication
@EnableScheduling
public class HomeworkApplication {

    /**
     * Точка входа в приложение.
     * <p>
     * Запускает Spring Boot‑контекст, инициализирует все бины, настраивает веб‑сервер
     * (если включён модуль web) и активирует планировщик задач.
     * </p>
     *
     * @param args аргументы командной строки (обычно не используются напрямую)
     */
    public static void main(String[] args) {
        SpringApplication.run(HomeworkApplication.class, args);
    }
}
