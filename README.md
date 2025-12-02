# Каталог книг (мульти-модульний Maven)

Багатомодульний застосунок на **Java 21 + Servlets (Jetty 11) + JDBC + H2** із трьома модулями:
- `core` — доменні моделі, порти, бізнес-правила.
- `persistence` — JDBC/H2-реалізації портів, ініціалізація схеми.
- `web` — сервлети та HTTP API (war), запуск через Jetty.

## Вимоги
- JDK 21
- Maven 3.9+
- Порт за замовчуванням: `8080`

## Збірка та запуск
```bash
# Запуск веб-модуля на Jetty 11
mvn -pl web -am jetty:run
```

Змінні середовища для БД (необов’язково):
- `DB_URL` (default `jdbc:h2:file:./data/library;AUTO_SERVER=TRUE`)
- `DB_USER` (default `sa`)
- `DB_PASSWORD` (default ``)
- `DB_INIT_DATA` (`true|false`, за замовчуванням true — заповнюється базовими книгами)

## HTTP API (UTF-8 + application/json)
- `GET /books?q=&page=&size=&sort=` — список книг, сортування по `id|title|author` (`sort=title,desc`).
- `GET /books/{id}` — картка книги + сторінка коментарів (`page`/`size` для коментарів).
- `GET /books/{id}/comments` — пагіновані коментарі до книги.
- `POST /books/{id}/comments` — створити коментар: `{"author": "...", "text": "..."}` (валідація: `author<=64`, `text<=1000`).
- `DELETE /comments/{id}` — видалити коментар (доступно лише протягом 24 годин після створення, інакше `409 Conflict`).

Коди відповіді: `200/201/204` успіх, `400` валідація/некоректні параметри, `404` не знайдено, `409` бізнес-конфлікт, `500` неочікувана помилка.

Формат помилки (єдиний):
```json
{
  "timestamp": "2024-02-02T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/books/1/comments",
  "details": {
    "author": "required, up to 64 characters"
  }
}
```

## Логування
- SLF4J + Logback, console appenders, рівень ROOT=INFO.
- INFO: створення/видалення коментарів з ключовими полями.
- WARN: усі 4xx у веб-шарі, ERROR: 5xx.

## Архітектурні правила (ArchUnit)
- `web` не залежить від `persistence`.
- `core` не залежить від Servlet/JDBC.
- Контролери лише в `web`, репозиторії/DAO лише в `persistence`.

Запуск тесту:
```bash
mvn -pl web -am test -Dtest=ArchitectureTest
```
