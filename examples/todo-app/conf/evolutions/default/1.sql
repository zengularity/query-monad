# --- !Ups

CREATE TABLE user (
    id NUMBER NOT NULL,
    full_name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE todo (
    id NUMBER NOT NULL,
    content varchar(255) NOT NULL,
    author_id NUMBER NOT NULL,
    done boolean NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE todo;
