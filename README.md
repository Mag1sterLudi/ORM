# Итоговый проект по ORM - учебная платформа

Учебная платформа для онлайн-курса по ORM и Hibernate. Курсы состоят из модулей и уроков. К урокам
выдаются задания. К модулям - тесты. Студенты записываются на курсы, сдают решения, проходят тесты
и оставляют отзывы.

Стек - Java 17, Spring Boot 3.5, Spring Data JPA (Hibernate 6), PostgreSQL, H2 для тестов,
Swagger (springdoc), Lombok, JUnit 5, Mockito, JaCoCo, Checkstyle, Docker, GitHub Actions.

## Запуск

### Через Docker

```bash
docker compose up --build
```

Поднимутся PostgreSQL и приложение, схема БД создастся автоматически, база заполнится демо-данными.

- API: http://localhost:8080/api/courses
- Swagger UI: http://localhost:8080/swagger-ui.html

Если порт 5432 уже занят локальным PostgreSQL, необходимо скопировать `.env.example` в `.env` и поменять `DB_PORT`.

### С локальным PostgreSQL

Нужны JDK 17+ и PostgreSQL. Maven устанавливать не обязательно, есть Maven Wrapper.

```bash
createdb learning_platform
```

```bash
DB_USERNAME=postgres DB_PASSWORD=postgres ./mvnw spring-boot:run
```

### Сборка и тесты

```bash
./mvnw verify
```

Запускает Checkstyle, все тесты (на H2, PostgreSQL не нужен) и делает отчёт о покрытии
`target/site/jacoco/index.html`.

## Конфигурация

Настройки лежат в `src/main/resources/application*.yml`, параметры подключения к БД передаются
через переменные окружения `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. В коде их нет.

- `dev` (по умолчанию) - локальный PostgreSQL со значениями по умолчанию, SQL в логе, демо-данные;
- `prod` - все параметры БД обязательны и берутся только из окружения, демо-данных нет;
- `test` - H2 в режиме совместимости с PostgreSQL, схема пересоздаётся при каждом запуске.

Схема БД создаётся и обновляется Hibernate (`ddl-auto: update`).

## Структура проекта

Слои: контроллеры -> сервисы -> репозитории -> сущности. Контроллеры принимают и отдают только DTO,
поэтому сущности и ленивые прокси не попадают в JSON.

```
src/main/java/com/learning/platform
├── config/       демо-данные, настройка Swagger
├── entity/       JPA-сущности и enumы
├── repository/   Spring Data репозитории
├── service/      бизнес-логика и транзакции
├── dto/          запросы (с валидацией) и ответы
├── web/          REST-контроллеры
└── exception/    исключения и GlobalExceptionHandler
```

Ошибки обрабатываются в одном месте (`GlobalExceptionHandler`) и возвращаются в едином формате
(`timestamp`, `status`, `error`, `message`, `path`, при ошибках валидации ещё `fieldErrors`):
404 - объект не найден, 400 - неверные данные или нарушение правила (например, не та роль),
409 - дубликат или конфликт (повторная запись на курс, удаление преподавателя с курсами).

## Модель данных

16 сущностей, у всех суррогатный ключ `id` из общего суперкласса `BaseEntity`.

- `User` - пользователь с ролью STUDENT, TEACHER или ADMIN; `Profile` - его профиль (1-1).
- `Course` - курс: категория `Category` (M-1), преподаватель `User` (M-1), теги `Tag` (M-M через таблицу `course_tag`).
- `CourseModule` -> `Lesson` -> `Assignment` -> `Submission` - структура курса и решения студентов (1-M на каждом уровне).
- `Quiz` - тест модуля (1-1), `Question` и `AnswerOption` - вопросы и варианты, `QuizSubmission` - результаты.
- `Enrollment` - запись студента на курс, сущность-связка M-M между `User` и `Course` со статусом и датой.
- `CourseReview` - отзыв о курсе, `Notification` - уведомление пользователя.

Уникальные ограничения в БД - email пользователя; одна запись на курс, одно решение на задание и один
отзыв на курс от студента; один тест на модуль.

Класс модуля назван `CourseModule`, чтобы не путать его с `java.lang.Module`.

Что происходит при удалении:
- курс удаляется вместе с модулями, уроками, заданиями, решениями, тестами, записями и отзывами,
  теги остаются (удаляются только строки в `course_tag`);
- пользователь удаляется вместе с профилем, записями, решениями, результатами, отзывами и
  уведомлениями, но преподавателя, у которого есть курсы, удалить нельзя (409);
- при удалении категории курсы остаются без категории.

## Ленивая загрузка

Все связи объявлены как `LAZY`, в том числе `@ManyToOne` и `@OneToOne`. Open Session In View отключён,
поэтому обращение к незагруженной связи после закрытия сессии бросает `LazyInitializationException`.
Это показано в тесте [`LazyLoadingTest`](src/test/java/com/learning/platform/repository/LazyLoadingTest.java).

Как это решается:
- `JOIN FETCH` в запросах репозиториев (например, `CourseRepository.findWithModulesById`);
- `@EntityGraph` для карточки и списка курсов;
- преобразование в DTO внутри `@Transactional` методов сервисов;
- `default_batch_fetch_size: 50`, чтобы ленивые связи догружались пачками, а не по одной (защита от N+1).

Обратные стороны `@OneToOne` (`User.profile`, `CourseModule.quiz`) Hibernate без bytecode enhancement
загружает сразу, поэтому тест модуля подгружается тем же `JOIN FETCH`, что и модули.

## REST API

Полная документация в Swagger UI: http://localhost:8080/swagger-ui.html. `POST` возвращает 201,
`DELETE` - 204.

Готовые запросы по основным сценариям лежат в [`requests.http`](requests.http). Их удобно запускать
по порядку на чистой базе с демо-данными в IntelliJ IDEA Ultimate (встроенный HTTP Client). Без неё
те же запросы можно выполнить через Swagger UI или curl. Тела запросов и ожидаемые коды ответов
указаны в файле, id задания и решения нужно подставить вручную из предыдущих ответов.

Пользователи:

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/users` | создать пользователя вместе с профилем |
| GET | `/api/users?role=STUDENT` | список, фильтр по роли необязателен |
| GET/PUT/DELETE | `/api/users/{id}` | пользователь с профилем / изменить / удалить |
| PUT | `/api/users/{id}/profile` | изменить профиль |
| GET | `/api/users/{id}/courses` | курсы студента |
| GET | `/api/users/{id}/submissions` | решения студента с оценками |
| GET | `/api/users/{id}/quiz-results` | результаты тестов студента |
| GET | `/api/users/{id}/notifications` | уведомления |

Категории и теги:

| Метод | Путь | Описание |
|-------|------|----------|
| GET/POST | `/api/categories` | список / создать |
| GET/PUT/DELETE | `/api/categories/{id}` | получить / переименовать / удалить |
| GET/POST | `/api/tags` | список / создать |
| DELETE | `/api/tags/{id}` | удалить |

Курсы:

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/courses` | создать курс, можно сразу с модулями и уроками |
| GET | `/api/courses?categoryId=&teacherId=&tag=` | поиск курсов |
| GET/PUT/DELETE | `/api/courses/{id}` | карточка курса / изменить / удалить |
| GET | `/api/courses/{id}/structure` | структура: модули, уроки, тесты |
| POST/GET | `/api/courses/{id}/modules` | добавить модуль / список модулей |
| POST/DELETE | `/api/courses/{id}/enroll?userId={id}` | записать / отписать студента |
| POST | `/api/courses/{id}/complete?userId={id}` | отметить курс пройденным |
| GET | `/api/courses/{id}/students` | студенты курса |
| POST/GET | `/api/courses/{id}/reviews` | оставить отзыв / список отзывов |

Модули, уроки, задания:

| Метод | Путь | Описание |
|-------|------|----------|
| GET/PUT/DELETE | `/api/modules/{id}` | модуль |
| POST/GET | `/api/modules/{id}/lessons` | добавить урок / уроки модуля |
| POST | `/api/modules/{id}/quiz` | создать тест модуля |
| GET/PUT/DELETE | `/api/lessons/{id}` | урок |
| POST/GET | `/api/lessons/{id}/assignments` | создать задание / задания урока |
| GET/PUT/DELETE | `/api/assignments/{id}` | задание |
| POST | `/api/assignments/{id}/submit` | отправить решение `{studentId, content}` |
| GET | `/api/assignments/{id}/submissions` | решения по заданию |
| GET | `/api/submissions/{id}` | решение |
| PUT | `/api/submissions/{id}/grade` | оценить `{score, feedback}` |

Тесты, отзывы, уведомления:

| Метод | Путь | Описание |
|-------|------|----------|
| GET/PUT/DELETE | `/api/quizzes/{id}` | тест (без правильных ответов) / изменить / удалить |
| POST | `/api/quizzes/{id}/take` | пройти тест `{studentId, answers: {questionId: [optionId]}}` |
| GET | `/api/quizzes/{id}/results` | результаты теста |
| DELETE | `/api/reviews/{id}` | удалить отзыв |
| PUT | `/api/notifications/{id}/read` | отметить уведомление прочитанным |

Вопрос теста засчитывается, если выбраны все правильные варианты и ни одного
неправильного. Результат - процент
правильных ответов, тест сдан, если он не ниже проходного балла (по умолчанию 60).
Пересдавать можно.

Пример через curl (на базе с демо-данными):

```bash
curl -s -X POST localhost:8080/api/users -H 'Content-Type: application/json' \
  -d '{"name":"Student","email":"student@mail.com","role":"STUDENT"}'
curl -s -X POST "localhost:8080/api/courses/1/enroll?userId=7"
curl -s -X POST localhost:8080/api/assignments/1/submit -H 'Content-Type: application/json' \
  -d '{"studentId":7,"content":"Моё решение"}'
```

## Демо-данные

При первом запуске с пустой БД (профиль `dev`) создаются 6 пользователей (администратор,
преподаватели id 2 и 3, студенты id 4, 5, 6), 2 курса с модулями и уроками, задания, решения,
тест и отзыв. Например, можно открыть `GET /api/courses/1/structure` или `GET /api/users/4/submissions`.

## Тесты

61 тест, покрытие около 75%. Интеграционные тесты поднимают Spring-контекст (`@SpringBootTest`)
на H2, каждый тест откатывается после выполнения.

- `repository` - создание схемы, CRUD через репозитории, каскады, уникальные ограничения, ленивая загрузка;
- `service` - сценарии курсов, записи на курс, заданий, тестов, пользователей и отзывов;
- `web` - сквозной сценарий через REST и коды ответов 201/204/400/404/409;
- `unit` - юнит-тесты без Spring: подсчёт баллов теста, логика сущностей, сервис с Mockito.

```bash
./mvnw test -Dtest=LazyLoadingTest
```

## CI и Docker

- GitHub Actions (`.github/workflows/ci.yml`) на каждый push запускает `mvn verify`
  (Checkstyle, тесты, отчёт JaCoCo) и собирает Docker-образ.
- Checkstyle (`config/checkstyle/checkstyle.xml`) проверяет стиль при сборке, при нарушениях сборка падает.
- `Dockerfile` собирает приложение в два этапа, `docker-compose.yml` поднимает его вместе с PostgreSQL.
