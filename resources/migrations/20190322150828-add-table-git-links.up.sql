CREATE TABLE repository_providers
(
    ID           integer      not null auto_increment primary key,
    name         VARCHAR(191),
    access_token VARCHAR(255) not null,
    CONSTRAINT UC_Provider UNIQUE (name)
);
