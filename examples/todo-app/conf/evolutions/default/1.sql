# --- !Ups

CREATE TABLE users (
    id VARCHAR(50) NOT NULL,
    login VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE todos (
    id VARCHAR(50) NOT NULL,
    todo_number INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    author_id VARCHAR(50) NOT NULL,
    done BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE credentials (
    login VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    PRIMARY KEY (login)
);

# --- !Downs

DROP TABLE todos;
DROP TABLE users;
DROP TABLE credentials;
