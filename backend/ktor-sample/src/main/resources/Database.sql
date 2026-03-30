CREATE TABLE workouts (
    workoutID INTEGER PRIMARY KEY AUTOINCREMENT,
    createdBy INTEGER NOT NULL,
    assignedTo INTEGER NOT NULL,
    date TEXT NOT NULL,
    duration INTEGER,
    workoutType TEXT,
    FOREIGN KEY (createdBy) REFERENCES users(userID),
    FOREIGN KEY (assignedTo) REFERENCES users(userID)
);

/* Join table */
CREATE TABLE workout_exercises (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutID INTEGER NOT NULL,
    exerciseID INTEGER NOT NULL,
    sets INTEGER,
    reps INTEGER,
    weight REAL,
    duration REAL,
    FOREIGN KEY (workoutID) REFERENCES workouts(workoutID),
    FOREIGN KEY (exerciseID) REFERENCES exercises(exerciseID)
);

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

CREATE TABLE exercises (
    exerciseID INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    description TEXT
);