CREATE TABLE users
(id INTEGER PRIMARY KEY,
name VARCHAR(30)
id_zeus INTEGER UNIQUE,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(30),
 admin BOOLEAN,
 last_login TIMESTAMP,
 is_active BOOLEAN,
 pass VARCHAR(300)
 access_token VARCHAR(255));
