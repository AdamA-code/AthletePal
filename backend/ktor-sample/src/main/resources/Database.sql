DROP TABLE IF EXISTS users;

CREATE TABLE users (
    userID INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NUll,
    email TEXT NOT NULL UNIQUE,
    passwordHash TEXT NOT NUll,
    passwordKey TEXT NOT NULL,
    gender TEXT,
    age INTEGER,
    height REAL,
    weight REAL,
    accountType BOOLEAN
    );