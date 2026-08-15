# Дипломная работа по курсу "JAVA-разработчик"

Сервис для управления объявлениями, пользователями и комментариями с хранением файлов в Alfresco.

## Стек технологий

- **Java 21**, Spring Boot 4.1.0
- **PostgreSQL 16** — основная база данных
- **Liquibase** — миграции схемы базы данных
- **Spring Data JPA** — работа с PostgreSQL
- **Spring Security (Basic Auth)** — авторизация и аутентификация
- **Swagger/OpenAPI (SpringDoc 2.8.5)** — документация API
- **MapStruct** — маппинг DTO/сущностей
- **Lombok** — генерация бойлерплейта
- **Alfresco Community 23.x** — файловое хранилище (аватарки и картинки объявлений)
- **RestClient** — взаимодействие с Alfresco API

## Архитектура

Приложение построено по классической трёхуровневой архитектуре:

```
controller → service → repository (JPA → PostgreSQL)
              ↓
              filestorage (Alfresco)
```


### Функциональные модули

| Модуль                 | Описание                                                                                    |
|------------------------|---------------------------------------------------------------------------------------------|
| **Пользователи**       | Регистрация, авторизация, получение/обновление информации, смена пароля, обновление аватара |
| **Объявления**         | CRUD объявлений, получение всех/своих объявлений, обновление картинки                       |
| **Комментарии**        | CRUD комментариев к объявлениям                                                             |
| **Файловое хранилище** | Загрузка/замена/удаление файлов в Alfresco                                                  |

---

## API Endpoints

### Пользователи

| Метод | Путь | Описание | Авторизация  |
|-------|------|----------|--------------|
| POST | `/login` | Авторизация пользователя | ❌ Нет       |
| POST | `/register` | Регистрация пользователя | ❌ Нет       |
| POST | `/users/set_password` | Изменение пароля | ✅ Да        |
| GET | `/users/me` | Получение информации о пользователе | ✅ Да        |
| PATCH | `/users/me` | Обновление информации о пользователе | ✅ Да        |
| PATCH | `/users/me/image` | Обновление аватара | ✅ Да        |

### Рекламные объявления

| Метод | Путь | Описание | Авторизация |
|-------|------|----------|-------------|
| GET | `/ads` | Получение всех объявлений | ❌ Нет |
| POST | `/ads` | Добавление объявления | ✅ Да |
| GET | `/ads/{id}` | Получение информации об объявлении | ✅ Да |
| DELETE | `/ads/{id}` | Удаление объявления | ✅ Да |
| PATCH | `/ads/{id}` | Обновление информации об объявлении | ✅ Да |
| GET | `/ads/me` | Получение объявлений пользователя | ✅ Да |
| PATCH | `/ads/{id}/image` | Обновление картинки объявления | ✅ Да |

### Комментарии

| Метод | Путь | Описание | Авторизация |
|-------|------|----------|-------------|
| GET | `/ads/{id}/comments` | Получение комментариев объявления | ✅ Да |
| POST | `/ads/{id}/comments` | Добавление комментария | ✅ Да |
| DELETE | `/ads/{adId}/comments/{commentId}` | Удаление комментария | ✅ Да |
| PATCH | `/ads/{adId}/comments/{commentId}` | Обновление комментария | ✅ Да |

---

## Запуск проекта

### Требования

- **JDK 21**
- **PostgreSQL 16** (локально или в Docker)
- **Alfresco Community** (на порту 9090)
- **Maven** (или использовать встроенный ./mvnw)

---

### 1. Запуск PostgreSQL

#### Вариант А: Через Docker (рекомендуется)

```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=java_graduate_work \
  -e POSTGRES_USER=java_user \
  -e POSTGRES_PASSWORD=java \
  -p 5432:5432 \
  postgres:16
```

#### Вариант Б: Локальная установка PostgreSQL

    Установите PostgreSQL 16
    Создайте базу данных:
CREATE DATABASE java_graduate_work;
CREATE USER java_user WITH PASSWORD 'java';
GRANT ALL PRIVILEGES ON DATABASE java_graduate_work TO java_user;

### 2. Запуск Alfresco Community

Alfresco должен быть запущен на порту 9090.

#### Вариант А: Через Docker Compose (рекомендуется)

Создайте docker-compose-alfresco.yml:
````
version: '3'
services:
alfresco:
image: alfresco/alfresco-content-repository-community:23.1.0
ports:
- "9090:8080"
environment:
- JAVA_OPTS=-Xms512m -Xmx1024m
volumes:
- alfresco_data:/usr/local/tomcat/alf_data
volumes:
alfresco_data:
````

  Запустите:
```bash
  docker-compose -f docker-compose-alfresco.yml up -d
```

#### Вариант Б: Скачать и запустить Alfresco вручную

Скачайте Alfresco Community с официального сайта
Распакуйте и запустите:
```bash
cd alfresco-community
./alfresco.sh start
```

Подготовка папки в Alfresco
После запуска Alfresco:
```
    Откройте: http://localhost:9090/share

    Войдите: admin / admin

    Перейдите в Репозиторий (Repository)

    Создайте папку storage (или любое другое название)

    Скопируйте ID папки (UUID) в application.properties:

        alfresco.folder-id=ваш-id-папки
```
### 4. Сборка и запуск приложения

Через Maven Wrapper (рекомендуется):
### Сборка
./mvnw clean compile

### Запуск
./mvnw spring-boot:run

Через IDEA:

    Откройте проект в IntelliJ IDEA
    Запустите HomeworkApplication.java


### 5. Проверка работы
   Сервис	URL	Логин/Пароль
   Приложение	http://localhost:8080	-
   Swagger UI	http://localhost:8080/swagger-ui.html	-
   Alfresco Share	http://localhost:9090/share	admin / admin


### 6. Тестирование через Swagger
    Откройте Swagger UI: http://localhost:8080/swagger-ui.html
    Нажмите кнопку "Authorize" (🔒)
    Введите логин и пароль (например, admin / admin)
    Выполняйте запросы к защищенным эндпоинтам

### 7. База данных
Liquibase автоматически применяет миграции при запуске приложения.

  Файлы миграций:

src/main/resources/db/changelog/
  ├── db.changelog-master.yaml          # Главный файл
  └── versions/
  ├── v1-create-tables.yaml          # Создание таблиц
  └── v2-alter-file-id-length.yaml   # Увеличение длины полей ID

Схема базы данных

Таблицы:

    user — пользователи (email, пароль, имя, фамилия, телефон, роль, аватар)
    advertising — объявления (автор, цена, заголовок, описание, картинка)
    advertising_comments — комментарии к объявлениям (автор, текст, дата)

### 8. Структура проекта
````
src/main/java/ru/skypro/homework/
├── config/                    # Конфигурации
│   ├── WebSecurityConfig      # Spring Security
│   ├── SwaggerConfig          # Swagger/OpenAPI
│   └── AlfrescoProperties     # Настройки Alfresco
├── controller/                # REST контроллеры
│   ├── UserController
│   ├── AdsController
│   └── AuthController
├── dto/                       # DTO для API
├── exception/                 # Обработка ошибок
├── mapper/                    # MapStruct мапперы
├── model/                     # JPA сущности
├── repository/                # Spring Data JPA репозитории
├── security/                  # Spring Security
│   └── SecurityHelper         # Вспомогательные методы
├── service/                   # Бизнес-логика
│   ├── UserService
│   ├── AdvertisingService
│   ├── CommentService
│   └── storage/               # Файловое хранилище
│       ├── FileStorageService # Интерфейс
│       └── alfresco/          # Реализация на Alfresco
└── HomeworkApplication.java   # Точка входа
````

### 9. Возможные проблемы и решения
#### 9.1 Alfresco не запускается

Проверьте, что порт 9090 свободен:
netstat -ano | findstr :9090

#### 9.2 Ошибка подключения к PostgreSQL

Убедитесь, что PostgreSQL запущен и доступен по адресу localhost:5432.

#### 9.3 Swagger не открывается

Проверьте настройки в application.properties:

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true

#### 9.4 401 Unauthorized в Swagger

    Нажмите кнопку Authorize (🔒)
    Введите логин и пароль
    Нажмите Authorize

## Docker Compose (запуск всего проекта)

### Если хотите запустить всё в Docker:
````
docker-compose.yml

version: '3'
services:
postgres:
image: postgres:16
environment:
POSTGRES_DB: java_graduate_work
POSTGRES_USER: java_user
POSTGRES_PASSWORD: java
ports:
- "5432:5432"
volumes:
- postgres_data:/var/lib/postgresql/data

alfresco:
image: alfresco/alfresco-content-repository-community:23.1.0
ports:
- "9090:8080"
environment:
- JAVA_OPTS=-Xms512m -Xmx1024m
volumes:
- alfresco_data:/usr/local/tomcat/alf_data

volumes:
postgres_data:
alfresco_data:
````
Запуск:
````bash
docker-compose up -d
````
## Разработчик

    Студент: А.Батурин
    Курс: JAVA-разработчик
    Год: 2026