# Дипломная работа по курсу «JAVA-разработчик»

**Сервис для управления объявлениями, пользователями и комментариями с хранением файлов в Alfresco Content Services.**

---

## Стек технологий

* **Java 21**, **Spring Boot 4.1.0**
* **PostgreSQL 16** — основная база данных
* **Liquibase** — миграции схемы базы данных
* **Spring Data JPA** — работа с PostgreSQL
* **Spring Security (Basic Auth)** — авторизация и аутентификация
* **Swagger/OpenAPI (SpringDoc 2.8.5)** — документация API
* **MapStruct** — маппинг DTO и сущностей
* **Jakarta Validation** для валидации данных
* **Lombok** — генерация шаблонного кода
* **Alfresco Content Services Community 26.x**(далее Alfresco CS) — файловое хранилище (аватарки и изображения объявлений)
* **RestClient** — взаимодействие с Alfresco CS API

---

## Архитектура

Приложение построено по классической трёхуровневой архитектуре:

```text
controller → service → repository (JPA → PostgreSQL)
              ↓
          filestorage (Alfresco CS)
```
  Для обеспечения согласованности между реляционной базой данных и внешним файловым хранилищем Alfresco 
реализован механизм компенсационных операций. Новый файл загружается до фиксации изменений в БД, 
старый файл удаляется только после успешного commit транзакции, а при rollback выполняется удаление вновь 
загруженного файла. 
  Дополнительно реализована периодическая очистка orphan-файлов для достижения eventual consistency между системами.
  Удаляются только orphan-файлы, не имеющие ссылок в базе данных и существующие более 24 часов. Дополнительная проверка времени создания позволяет избежать race condition между процессом загрузки файла в Alfresco, сохранением ссылки на него в базе данных и выполнением фоновой задачи очистки. Такой подход повышает надёжность механизма синхронизации между файловым хранилищем и БД и обеспечивает eventual consistency системы.

### Функциональные модули

| Модуль                 | Описание                                                                                      |
| ---------------------- |-----------------------------------------------------------------------------------------------|
| **Пользователи**       | Регистрация, авторизация, получение и обновление информации, смена пароля, обновление аватара |
| **Объявления**         | CRUD объявлений, получение всех и своих объявлений, обновление изображения                    |
| **Комментарии**        | CRUD комментариев к объявлениям                                                               |
| **Файловое хранилище** | Загрузка, замена и удаление файлов в Alfresco CS                                              |

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
* **Alfresco CS Community 26.x** — на порту `9090`
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

### 2. Запуск Alfresco Content Services Community

Alfresco CS должен быть доступен на порту `9090`.

1. Сккопируйте файл из проекта community-compose.yaml в какую-либо папку на своем компьютере
2. Перейдите в папку с файлом community-compose.yaml
3. Запустите

```bash
docker compose -f community-compose.yaml up -d
```
4. Дождитесь запуска всех контейнеров и подождите еще 3-5 минут для инициализации всех таблиц базы данных Alfresco CS

#### Подготовка папки в Alfresco CS

После запуска Alfresco CS:

1. Откройте `http://localhost:9091/share`.
2. Войдите с учётными данными `admin / admin`.
3. Перейдите в **Репозиторий (Repository)**.
4. Создайте папку `storage`(или используйте другое название), например, в папке `Sites`.
5. Скопируйте UUID созданной папки.
6. Укажите UUID в `application.properties`:

```properties
alfresco.folder-id=ваш-id-папки
```
---

### 3. Настройка приложения

Перед запуском приложения проверьте настройки подключения к PostgreSQL и Alfresco CS в файле:

```text
src/main/resources/application.properties
```

Убедитесь, что указаны корректные параметры подключения к базе данных и UUID папки в Alfresco CS.

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

| Сервис            | URL                                     | Логин / пароль  |
|-------------------|-----------------------------------------| --------------- |
| Приложение        | `http://localhost:8080`                 | —               |
| Swagger UI        | `http://localhost:8080/swagger-ui.html` | —               |
| Alfresco CS Share | `http://localhost:9091/share`           | `admin / admin` |

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
│   │   │   ├── AdvertisingCreationException.java
│   │   │   ├── AdvertisingDeletionException.java
│   │   │   ├── AdvertisingImageUpdateException.java
│   │   │   ├── AdvertisingNotFoundException.java
│   │   │   ├── AdvertisingRetrievalException.java
│   │   │   ├── AdvertisingUpdateException.java
│   │   │   ├── CommentNotFoundException.java
│   │   │   ├── FileStorageException.java
│   │   │   └── InvalidPasswordException.java
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
		│   ├── ImageControllerTest.java
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
* **`service/storage/alfresco`** — реализация файлового хранилища на базе Alfresco CS.
* **`config`** — конфигурация приложения и инфраструктурных компонентов.
* **`exception`** — пользовательские исключения.
* **`test`** — модульные и интеграционные тесты контроллеров, сервисов и мапперов.

---

### 9. Возможные проблемы и решения

#### 9.1. Alfresco CS не запускается

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

## Разработчик

**Студент:** Александр Батурин
**Курс:** JAVA-разработчик
**Год:** 2026
