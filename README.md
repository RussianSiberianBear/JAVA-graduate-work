# Дипломная работа по курсу «JAVA-разработчик»

**Сервис для управления объявлениями, пользователями и комментариями с хранением файлов в Alfresco.**

---

## Стек технологий

* **Java 21**, **Spring Boot 4.1.0**
* **PostgreSQL 16** — основная база данных
* **Liquibase** — миграции схемы базы данных
* **Spring Data JPA** — работа с PostgreSQL
* **Spring Security (Basic Auth)** — авторизация и аутентификация
* **Swagger/OpenAPI (SpringDoc 2.8.5)** — документация API
* **MapStruct** — маппинг DTO и сущностей
* **Lombok** — генерация шаблонного кода
* **Alfresco Community 23.x** — файловое хранилище (аватарки и изображения объявлений)
* **RestClient** — взаимодействие с Alfresco API

---

## Архитектура

Приложение построено по классической трёхуровневой архитектуре:

```text
controller → service → repository (JPA → PostgreSQL)
              ↓
          filestorage (Alfresco)
```

### Функциональные модули

| Модуль                 | Описание                                                                                      |
| ---------------------- | --------------------------------------------------------------------------------------------- |
| **Пользователи**       | Регистрация, авторизация, получение и обновление информации, смена пароля, обновление аватара |
| **Объявления**         | CRUD объявлений, получение всех и своих объявлений, обновление изображения                    |
| **Комментарии**        | CRUD комментариев к объявлениям                                                               |
| **Файловое хранилище** | Загрузка, замена и удаление файлов в Alfresco                                                 |

---

## API Endpoints

### Пользователи

| Метод   | Путь                  | Описание                             | Авторизация |
| ------- | --------------------- | ------------------------------------ | ----------- |
| `POST`  | `/login`              | Авторизация пользователя             | ❌ Нет       |
| `POST`  | `/register`           | Регистрация пользователя             | ❌ Нет       |
| `POST`  | `/users/set_password` | Изменение пароля                     | ✅ Да        |
| `GET`   | `/users/me`           | Получение информации о пользователе  | ✅ Да        |
| `PATCH` | `/users/me`           | Обновление информации о пользователе | ✅ Да        |
| `PATCH` | `/users/me/image`     | Обновление аватара                   | ✅ Да        |

### Рекламные объявления

| Метод    | Путь              | Описание                            | Авторизация |
| -------- | ----------------- | ----------------------------------- | ----------- |
| `GET`    | `/ads`            | Получение всех объявлений           | ❌ Нет       |
| `POST`   | `/ads`            | Добавление объявления               | ✅ Да        |
| `GET`    | `/ads/{id}`       | Получение информации об объявлении  | ✅ Да        |
| `DELETE` | `/ads/{id}`       | Удаление объявления                 | ✅ Да        |
| `PATCH`  | `/ads/{id}`       | Обновление информации об объявлении | ✅ Да        |
| `GET`    | `/ads/me`         | Получение объявлений пользователя   | ✅ Да        |
| `PATCH`  | `/ads/{id}/image` | Обновление изображения объявления   | ✅ Да        |

### Комментарии

| Метод    | Путь                               | Описание                          | Авторизация |
| -------- | ---------------------------------- | --------------------------------- | ----------- |
| `GET`    | `/ads/{id}/comments`               | Получение комментариев объявления | ✅ Да        |
| `POST`   | `/ads/{id}/comments`               | Добавление комментария            | ✅ Да        |
| `DELETE` | `/ads/{adId}/comments/{commentId}` | Удаление комментария              | ✅ Да        |
| `PATCH`  | `/ads/{adId}/comments/{commentId}` | Обновление комментария            | ✅ Да        |

---

## Запуск проекта

### Требования

* **JDK 21**
* **PostgreSQL 16** — локально или в Docker
* **Alfresco Community 23.x** — на порту `9090`
* **Maven** или встроенный Maven Wrapper (`./mvnw`)

---

### 1. Запуск PostgreSQL

#### Вариант А: через Docker (рекомендуется)

```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=java_graduate_work \
  -e POSTGRES_USER=java_user \
  -e POSTGRES_PASSWORD=java \
  -p 5432:5432 \
  postgres:16
```

#### Вариант Б: локальная установка PostgreSQL

Установите PostgreSQL 16 и создайте базу данных и пользователя:

```sql
CREATE DATABASE java_graduate_work;
CREATE USER java_user WITH PASSWORD 'java';
GRANT ALL PRIVILEGES ON DATABASE java_graduate_work TO java_user;
```

---

### 2. Запуск Alfresco Community

Alfresco должен быть доступен на порту `9090`.

#### Вариант А: через Docker Compose (рекомендуется)

Создайте файл `docker-compose-alfresco.yml`:

```yaml
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
```

Запустите Alfresco:

```bash
docker-compose -f docker-compose-alfresco.yml up -d
```

#### Вариант Б: скачать и запустить Alfresco вручную

Скачайте Alfresco Community с официального сайта, распакуйте архив и запустите приложение:

```bash
cd alfresco-community
./alfresco.sh start
```

#### Подготовка папки в Alfresco

После запуска Alfresco:

1. Откройте `http://localhost:9090/share`.
2. Войдите с учётными данными `admin / admin`.
3. Перейдите в **Репозиторий (Repository)**.
4. Создайте папку `storage` (или используйте другое название).
5. Скопируйте UUID созданной папки.
6. Укажите UUID в `application.properties`:

```properties
alfresco.folder-id=ваш-id-папки
```

---

### 3. Настройка приложения

Перед запуском приложения проверьте настройки подключения к PostgreSQL и Alfresco в файле:

```text
src/main/resources/application.properties
```

Убедитесь, что указаны корректные параметры подключения к базе данных и UUID папки в Alfresco.

---

### 4. Сборка и запуск приложения

#### Через Maven Wrapper (рекомендуется)

Сборка:

```bash
./mvnw clean compile
```

Запуск:

```bash
./mvnw spring-boot:run
```

#### Через IntelliJ IDEA

1. Откройте проект в IntelliJ IDEA.
2. Найдите `HomeworkApplication.java`.
3. Запустите приложение.

---

### 5. Проверка работы

| Сервис         | URL                                     | Логин / пароль  |
| -------------- | --------------------------------------- | --------------- |
| Приложение     | `http://localhost:8080`                 | —               |
| Swagger UI     | `http://localhost:8080/swagger-ui.html` | —               |
| Alfresco Share | `http://localhost:9090/share`           | `admin / admin` |

---

### 6. Тестирование через Swagger

1. Откройте Swagger UI: `http://localhost:8080/swagger-ui.html`.
2. Нажмите кнопку **Authorize** (🔒).
3. Введите логин и пароль, например `admin / admin`.
4. Выполняйте запросы к защищённым эндпоинтам.

---

### 7. База данных

Liquibase автоматически применяет миграции при запуске приложения.

#### Файлы миграций

```text
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Главный файл миграций
└── versions/
    ├── v1-create-tables.yaml         # Создание таблиц
    └── v2-alter-file-id-length.yaml  # Увеличение длины полей ID
```

#### Схема базы данных

Основные таблицы:

* `user` — пользователи (email, пароль, имя, фамилия, телефон, роль, аватар)
* `advertising` — объявления (автор, цена, заголовок, описание, изображение)
* `advertising_comments` — комментарии к объявлениям (автор, текст, дата)

---

### 8. Структура проекта

```text
src/
├── main/
│   ├── java/ru/skypro/homework/
│   │   ├── HomeworkApplication.java          # Точка входа
│   │   │
│   │   ├── config/                            # Конфигурация приложения
│   │   │   ├── ApiExceptionHandler.java       # Обработка исключений API
│   │   │   ├── StorageDirectories.java        # Настройки директорий
│   │   │   ├── SwaggerConfig.java              # Swagger/OpenAPI
│   │   │   ├── WebConfig.java                  # Web-конфигурация
│   │   │   └── WebSecurityConfig.java          # Spring Security
│   │   │
│   │   ├── constants/                          # Константы приложения
│   │   │   └── ExceptionMessages.java           # Сообщения об ошибках
│   │   │
│   │   ├── controller/                         # REST-контроллеры
│   │   │   ├── AdsController.java              # Работа с объявлениями
│   │   │   ├── AuthController.java             # Авторизация
│   │   │   ├── ImageController.java            # Работа с изображениями
│   │   │   └── UserController.java             # Работа с пользователями
│   │   │
│   │   ├── dto/                                # DTO для API
│   │   │   ├── AdvertisingAllResponseDto.java
│   │   │   ├── AdvertisingOneResponseDto.java
│   │   │   ├── AdvertisingWithAuthorDto.java
│   │   │   ├── CommentOneResponseDto.java
│   │   │   ├── CommentRequestDto.java
│   │   │   ├── CommentsAllResponseDto.java
│   │   │   ├── CreateOrUpdateAd.java
│   │   │   ├── Login.java
│   │   │   ├── Register.java
│   │   │   ├── Role.java
│   │   │   ├── SetPasswordRequestDto.java
│   │   │   ├── UserInfoResponseDto.java
│   │   │   └── UserUpdateInfoDto.java
│   │   │
│   │   ├── exception/                          # Пользовательские исключения
│   │   │   ├── AdvertisingNotFoundException.java
│   │   │   ├── CommentNotFoundException.java
│   │   │   ├── InvalidPasswordException.java
│   │   │   └── UsernameNotFoundException.java
│   │   │
│   │   ├── filter/                             # Фильтры HTTP-запросов
│   │   │   └── BasicAuthCorsFilter.java        # Basic Auth и CORS
│   │   │
│   │   ├── mapper/                             # MapStruct-мапперы
│   │   │   ├── AdvertisingMapper.java
│   │   │   ├── BaseMapper.java
│   │   │   ├── CommentMapper.java
│   │   │   └── UserMapper.java
│   │   │
│   │   ├── model/                              # JPA-сущности
│   │   │   ├── Advertising.java
│   │   │   ├── Comment.java
│   │   │   └── User.java
│   │   │
│   │   ├── repository/                         # Spring Data JPA
│   │   │   ├── AdvertisingRepository.java
│   │   │   ├── CommentRepository.java
│   │   │   └── UserRepository.java
│   │   │
│   │   ├── security/                           # Компоненты безопасности
│   │   │   ├── SecurityHelper.java
│   │   │   ├── UserDetailsImpl.java
│   │   │   └── UserDetailsServiceImpl.java
│   │   │
│   │   └── service/                            # Бизнес-логика
│   │       ├── AdvertisingService.java
│   │       ├── AuthService.java
│   │       ├── CommentService.java
│   │       ├── UserService.java
│   │       ├── impl/                           # Реализации сервисов
│   │       │   └── AuthServiceImpl.java
│   │       └── storage/                        # Файловое хранилище
│   │           ├── FileStorageService.java     # Интерфейс хранилища
│   │           ├── FileUploadRequest.java      # Данные загрузки файла
│   │           ├── StoredFile.java             # Загруженный файл
│   │           ├── StoredFileInfo.java         # Информация о файле
│   │           └── alfresco/                   # Интеграция с Alfresco
│   │               ├── AlfrescoChildrenResponse.java
│   │               ├── AlfrescoCleanupJob.java
│   │               ├── AlfrescoConfig.java
│   │               ├── AlfrescoFileInfo.java
│   │               ├── AlfrescoFileStorageService.java
│   │               ├── AlfrescoProperties.java
│   │               └── AlfrescoResponse.java
│   │
│   └── resources/
│       ├── application.properties               # Настройки приложения
│       ├── liquibase.properties                 # Настройки Liquibase
│       └── db/
│           └── changelog/
│               ├── db.changelog-master.yaml     # Главный файл миграций
│               └── versions/
│                   ├── v1-create-tables.yaml   # Создание таблиц
│                   └── v2-alter-file-id-length.yaml
│
└── test/
    └── java/ru/skypro/homework/
        ├── HomeworkApplicationTests.java       # Тест запуска приложения
        ├── controller/                         # Тесты контроллеров
        │   ├── AdsControllerTest.java
        │   ├── AuthControllerTest.java
        │   └── UserControllerTest.java
        ├── mapper/                             # Тесты мапперов
        │   ├── AdvertisingMapperTest.java
        │   ├── CommentMapperTest.java
        │   └── UserMapperTest.java
        └── service/                            # Тесты сервисов
            ├── AdvertisingServiceTest.java
            ├── AuthServiceImplTest.java
            ├── CommentServiceTest.java
            └── UserServiceTest.java
```

Структура проекта разделена на основные уровни:

* **`controller`** — REST API и обработка HTTP-запросов.
* **`service`** — бизнес-логика приложения.
* **`repository`** — доступ к данным через Spring Data JPA.
* **`model`** — JPA-сущности базы данных.
* **`dto`** — объекты передачи данных между API и внутренними слоями приложения.
* **`mapper`** — преобразование DTO и JPA-сущностей с помощью MapStruct.
* **`security`** — аутентификация и работа с пользователями Spring Security.
* **`filter`** — дополнительные HTTP-фильтры, включая CORS и Basic Authentication.
* **`service/storage`** — абстракция файлового хранилища.
* **`service/storage/alfresco`** — реализация файлового хранилища на базе Alfresco.
* **`config`** — конфигурация приложения и инфраструктурных компонентов.
* **`exception`** — пользовательские исключения.
* **`test`** — модульные и интеграционные тесты контроллеров, сервисов и мапперов.

---

### 9. Возможные проблемы и решения

#### 9.1. Alfresco не запускается

Проверьте, что порт `9090` свободен:

```cmd
netstat -ano | findstr :9090
```

#### 9.2. Ошибка подключения к PostgreSQL

Убедитесь, что PostgreSQL запущен и доступен по адресу:

```text
localhost:5432
```

Также проверьте имя базы данных, пользователя и пароль в `application.properties`.

#### 9.3. Swagger не открывается

Проверьте настройки в `application.properties`:

```properties
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
```

#### 9.4. Ошибка `401 Unauthorized` в Swagger

1. Нажмите кнопку **Authorize** (🔒).
2. Введите логин и пароль.
3. Нажмите **Authorize**.

---

## Docker Compose

### Запуск PostgreSQL и Alfresco

Для запуска инфраструктуры проекта в Docker Compose создайте файл `docker-compose.yml`:

```yaml
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
```

Запуск:

```bash
docker-compose up -d
```

---

## Разработчик

**Студент:** Александр Батурин
**Курс:** JAVA-разработчик
**Год:** 2026
