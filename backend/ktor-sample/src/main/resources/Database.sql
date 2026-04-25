CREATE TABLE IF NOT EXISTS training_groups (
    groupID INTEGER PRIMARY KEY AUTOINCREMENT,
    createdBy INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    goal TEXT,
    FOREIGN KEY(createdBy) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS group_members (
    memberID INTEGER PRIMARY KEY AUTOINCREMENT,
    groupID INTEGER NOT NULL,
    userID INTEGER NOT NULL,
    FOREIGN KEY(groupID) REFERENCES training_groups(groupID) ON DELETE CASCADE,
    FOREIGN KEY(userID) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS group_sessions (
    sessionID INTEGER PRIMARY KEY AUTOINCREMENT,
    groupID INTEGER NOT NULL,
    workoutID INTEGER NOT NULL,
    scheduledDate TEXT NOT NULL,
    FOREIGN KEY(groupID) REFERENCES training_groups(groupID) ON DELETE CASCADE,
    FOREIGN KEY(workoutID) REFERENCES workouts(workoutID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    tokenID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    token TEXT NOT NULL,
    expiry TEXT NOT NULL,
    used INTEGER DEFAULT 0, --prevents reuse
    FOREIGN KEY (userID) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routes (
    routeID INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    name TEXT NOT NULL,
    distance REAL NOT NULL,
    estimatedTime REAL,
    date TEXT NOT NULL,
    isPublic INTEGER NOT NULL DEFAULT 0,
    routeData TEXT NOT NULL,
    FOREIGN KEY(userID) REFERENCES users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS competition_exercises (
    competitionExerciseID INTEGER PRIMARY KEY AUTOINCREMENT,
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
    result INTEGER,
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
    notes TEXT,
    routeID INTEGER,
    FOREIGN KEY (routeID) REFERENCES routes(routeID) ON DELETE CASCADE,
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