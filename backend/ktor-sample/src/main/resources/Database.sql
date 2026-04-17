CREATE TABLE IF NOT EXISTS password_reset_tokens (
    tokenID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    token TEXT NOT NULL,
    expiry TEXT NOT NULL, -- datetime the token expires after 1 hour
    used INTEGER DEFAULT 0, -- 0 = not used, 1 = already used, prevents reuse
    FOREIGN KEY (userID) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS calendar_events (
    calendarID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    eventType TEXT NOT NULL, 
    eventID INTEGER NOT NULL,
    status TEXT NOT NULL,
    scheduledDate TEXT NOT NULL,
    name TEXT NOT NULL,
    FOREIGN KEY(userID) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routes (
    routeID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    name TEXT NOT NULL,
    distance REAL NOT NULL,
    estimatedTime REAL,
    date TEXT NOT NULL,
    FOREIGN KEY(userID) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS competition_exercises (
    competitionExercisesID INTEGER PRIMARY KEY AUTOINCREMENT,
    competitionID INTEGER NOT NULL,
    exerciseID INTEGER NOT NULL,
    performance REAL,
    FOREIGN KEY(competitionID) REFERENCES competitions(competitionID) ON DELETE CASCADE,
    FOREIGN KEY(exerciseID) REFERENCES exercises(exerciseID)
);

CREATE TABLE IF NOT EXISTS competitions (
    competitionID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    name TEXT NOT NULL,
    date TEXT NOT NULL,
    rank INTEGER,
    result Integer,
    FOREIGN KEY(userID) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS statistics (
    statisticID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    exerciseID INTEGER NOT NULL,
    scoreValue REAL NOT NULL,
    date TEXT NOT NULL,
    FOREIGN KEY(userID) REFERENCES users(userID) ON DELETE CASCADE,
    FOREIGN KEY(exerciseID) REFERENCES exercises(exerciseID)
);

CREATE TABLE IF NOT EXISTS workouts (
    workoutID INTEGER PRIMARY KEY AUTOINCREMENT,
    createdBy INTEGER NOT NULL,
    assignedTo INTEGER NOT NULL,
    date TEXT NOT NULL,
    duration INTEGER,
    workoutType TEXT,
    FOREIGN KEY (createdBy) REFERENCES users(userID) ON DELETE CASCADE,
    FOREIGN KEY (assignedTo) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS workout_exercises (
    workoutExercisesID INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutID INTEGER NOT NULL,
    exerciseID INTEGER NOT NULL,
    sets INTEGER,
    reps INTEGER,
    weight REAL,
    FOREIGN KEY (workoutID) REFERENCES workouts(workoutID) ON DELETE CASCADE,
    FOREIGN KEY (exerciseID) REFERENCES exercises(exerciseID)
);

CREATE TABLE IF NOT EXISTS users (
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

CREATE TABLE IF NOT EXISTS exercises (
    exerciseID INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    description TEXT
);