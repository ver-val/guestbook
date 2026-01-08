# Каталог книг (мульти-модульний Maven)

Багатомодульний застосунок на **Java 21 + Spring Boot (MVC/FreeMarker) + JPA/Flyway + H2** із трьома модулями:
- `core` — доменні моделі, порти, бізнес-правила.
- `persistence` — JPA/Flyway-реалізації портів, міграції БД.
- `web` — Spring MVC + FreeMarker (WAR, але запускається через Spring Boot).

## Вимоги
- JDK 21
- Maven 3.9+
- Порт за замовчуванням: `8080`

## Збірка та запуск
```bash
# Зібрати всі модулі й покласти артефакти в локальний кеш (.m2 у корені репо)
mvn -Dmaven.repo.local="$(pwd)/.m2" clean install

# (опційно) почистити H2, якщо треба оновити схему/дані
rm -f "$(pwd)/data/library".{mv,trace}.db

# Запустити веб-модуль (Spring Boot) зі шляху web/
cd web
mvn -Dmaven.repo.local="$(pwd)/../.m2" spring-boot:run
cd ..

## Міграції Flyway вручну (опційно)
```bash
export DB_URL="${DB_URL:-jdbc:h2:file:../data/library;AUTO_SERVER=TRUE}"
export DB_USER="${DB_USER:-sa}"
export DB_PASSWORD="${DB_PASSWORD:-}"

mvn -pl persistence -Dmaven.repo.local="$(pwd)/.m2" \
  org.flywaydb:flyway-maven-plugin:migrate \
  -Dflyway.url="$DB_URL" \
  -Dflyway.user="$DB_USER" \
  -Dflyway.password="$DB_PASSWORD"
```

Змінні середовища для БД (необов’язково):
- `DB_URL` (default `jdbc:h2:file:../data/library;AUTO_SERVER=TRUE`)
- `DB_USER` (default `sa`)
- `DB_PASSWORD` (default ``)
- Flyway запускає міграції автоматично на старті.

## Безпека та ролі
- Form-login `/login` / `/logout`, ролі `USER` та `ADMIN`.
- Доступ: `GET /books/**` — USER/ADMIN; `POST /books/*/comments` — USER/ADMIN; решта `/books/**` та `/api/**` — лише ADMIN; `GET /books/new` — лише ADMIN.
- Для `/api/**` 401/403 віддаються в JSON (без HTML), для MVC-сторінок — сторінки 401/403/404.
- Видалення коментаря: адмін — будь-який; користувач — лише свій і в межах 24 годин після створення (інакше 409).

## Підтвердження email
- Реєстрація створює користувача disabled, відправляє лист із токеном підтвердження.
- На екрані логіну показується підказка “Перевірте email, щоб завершити реєстрацію”.

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

## Email/FreeMarker
- Gmail SMTP конфігурується через env: `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `APP_MAIL_FROM`.
- HTML-шаблон листа: `web/src/main/resources/mail-templates/new_book.ftl` (логотип, рік, дата додавання, рідкісне видання, футер BookApp © 2025).
- Відправка листа на додавання книги виконується в `MailService`, виклик у `BookPageController` після успішного створення книги.

Приклад запуску з параметрами пошти:
```bash
export SPRING_MAIL_USERNAME="your@gmail.com"
export SPRING_MAIL_PASSWORD="your_16char_app_password"
export APP_MAIL_FROM="$SPRING_MAIL_USERNAME"

mvn -pl web -am -Dmaven.repo.local="$(pwd)/.m2" spring-boot:run
```

## Архітектурні правила (ArchUnit)
- `web` не залежить від `persistence`.
- `core` не залежить від Servlet/JDBC.
- Контролери лише в `web`, репозиторії/DAO лише в `persistence`.

Запуск тесту:
```bash
mvn -pl web -am test -Dtest=ArchitectureTest
```
