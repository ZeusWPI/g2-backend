CREATE TABLE repository_providers (
       ID INTEGER PRIMARY KEY,
       name VARCHAR(255),
       access_token VARCHAR(255),
       CONSTRAINT UC_Provider UNIQUE (name)
);
