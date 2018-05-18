# --- !Ups

CREATE TABLE users (
    id INT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE todos (
    id INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    author_id INT NOT NULL,
    done BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE todos;
DROP TABLE users;
