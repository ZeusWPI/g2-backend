CREATE TABLE users
(id INTEGER PRIMARY KEY,
zeus_id INTEGER UNIQUE,
name VARCHAR(255),
 email VARCHAR(255),
 admin BOOLEAN,
 last_login TIMESTAMP,
 access_token VARCHAR(255));
