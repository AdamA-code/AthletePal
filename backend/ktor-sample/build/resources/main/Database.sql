CREATE TABLE calendar_events (
    calendarID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    eventType TEXT NOT NULL, 
    eventID INTEGER NOT NULL,
    status TEXT NOT NULL,
    scheduledDate TEXT NOT NULL,
    name TEXT NOT NULL,
    FOREIGN KEY(userID) REFERENCES users(userID)
);

CREATE TABLE routes (
    routeID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    name TEXT NOT NULL,
    distance REAL NOT NULL,
    estimatedTime REAL,
    date TEXT NOT NULL,

    FOREIGN KEY(userID) REFERENCES users(userID)
);

CREATE TABLE competition_exercises (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    competitionID INTEGER NOT NULL,
    exerciseID INTEGER NOT NULL,
    performance REAL,
    FOREIGN KEY(competitionID) REFERENCES competitions(competitionID),
    FOREIGN KEY(exerciseID) REFERENCES exercises(exerciseID)
);

CREATE TABLE competitions (
    competitionID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    name TEXT NOT NULL,
    date TEXT NOT NULL,
    rank INTEGER,
    FOREIGN KEY(userID) REFERENCES users(userID)
);

CREATE TABLE statistics (
    statisticID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    exerciseID INTEGER NOT NULL,
    value REAL NOT NULL,
    date TEXT NOT NULL,
    FOREIGN KEY(userID) REFERENCES users(userID),
    FOREIGN KEY(exerciseID) REFERENCES exercises(exerciseID)
);

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

CREATE TABLE workout_exercises (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutID INTEGER NOT NULL,
    exerciseID INTEGER NOT NULL,
    sets INTEGER,
    reps INTEGER,
    weight REAL,
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
    type TEXT NOT NULL,
    description TEXT
);