INSERT INTO users (username, password, role) VALUES
    ('alice', '{noop}password', 'USER'),
    ('bob', '{noop}password', 'USER');

INSERT INTO books (title, author, description, pub_year) VALUES
    ('Clean Architecture', 'Robert C. Martin', 'Software craftsmanship and architectural principles.', 2017),
    ('Effective Java', 'Joshua Bloch', 'Best practices for Java programming.', 2018);

INSERT INTO comments (author, text, created_at, book_id, user_id) VALUES
    ('alice', 'Чудова книжка про архітектуру!', CURRENT_TIMESTAMP, 1, 1),
    ('bob', 'Полюбляю приклади з Effective Java.', CURRENT_TIMESTAMP, 2, 2);
