CREATE TABLE users
(
    id           INTEGER   not null auto_increment PRIMARY KEY,
    zeus_id      INTEGER UNIQUE,
    name         VARCHAR(255),
    email        VARCHAR(255),
    admin        BOOLEAN,
    last_login   TIMESTAMP null,
    access_token VARCHAR(255)
);
