package services
import java.sql.Connection // Interface for database connection
import java.sql.DriverManager // To connect to the DB
import java.sql.PreparedStatement // Precompiled SQL statement
import java.sql.ResultSet // Represents results of SQL queries
import java.sql.Statement

// data classes to finitely store inputs (and potentially outputs) to database

data class User(
    val userID: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    val passwordKey: String,
    val gender: String?,
    val age: Int?,
    val height: Double?,
    val weight: Double?,
    val accountType: Boolean
)

data class Exercise(
    val exerciseID: Int,
    val name: String,
    val category: String,
    val description: String?
)

data class Workout(
    val workoutID: Int,
    val createdBy: Int,
    val assignedTo: Int,
    val date: String,
    val duration: Int?
)

data class WorkoutExercise(
    val workoutExercisesID: Int,
    val workoutID: Int,
    val exerciseID: Int,
    val sets: Int?,
    val reps: Int?,
    val weight: Double?,
)

data class Statistic(
    val statisticID: Int,
    val userID: Int,
    val exerciseID: Int,
    val scoreValue: Double,
    val date: String,
)

data class Competition(
    val competitionID: Int,
    val userID: Int,
    val name: String,
    val date: String,
    val rank: Int?
)

data class CompetitionExercise(
    val competitionExerciseID: Int,
    val competitionID: Int,
    val exerciseID: Int,
    val performance: Double?
)

data class Route(
    val routeID: Int,
    val userID: Int,
    val name: String,
    val distance: Double,
    val estimatedTime: Double?,
    val date: String?
)

data class CalendarEvent(
    val calendarID: Int,
    val userID: Int,
    val eventType: String,
    val eventID: Int,
    val status: String,
    val scheduledDate: String,
    val name: String
)

// Connect to DB
class DatabaseService {
    //open connection to database
    val connection: Connection = DriverManager.getConnection("jdbc:sqlite:${System.getProperty("user.dir")}/src/main/resources/users.db")

    // Runs automatically on creation
    init {
        connection.createStatement().execute("PRAGMA foreign_keys = ON") // Enable deletion cascading
    }
}
val db = DatabaseService()
// User db

fun createUser(user: User): Int {
    if (findUser(user.email) == null) {
        // Connect to db, pepare query struct, use object that allows updating
        // and prevent SQL injection, store to set values and exec
        // SQL INSERT statement with "?"" as a placeholder
        val stmt: PreparedStatement = db.connection.prepareStatement(
            """
            INSERT INTO users(name, email, passwordHash, passwordKey, gender, age, height, weight, accountType)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS // Needed to return created ID
        )

        // Set the values for each placeholder in the SQL statement
        stmt.setString(1, user.name)
        stmt.setString(2, user.email)
        stmt.setString(3, user.passwordHash)
        stmt.setString(4, user.passwordKey)
        stmt.setString(5, user.gender)
        stmt.setObject(6, user.age)
        stmt.setObject(7, user.height)
        stmt.setObject(8, user.weight)
        stmt.setBoolean(9, user.accountType)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        return id
    }
    return -1
}

fun findUser(email: String): User? {
    // Connect to db, pepare query struct, use object that allows updating
    // and prevent SQL injection, store to set values and exec
    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM users WHERE email = ?"
    )

    stmt.setString(1, email) // Put email value in placeholder (?)

    val rs: ResultSet = stmt.executeQuery()

    // If query returns a row, create a User object
    if (rs.next()) {
        val user = User(
            userID = rs.getInt("userID"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            passwordHash = rs.getString("passwordHash"),
            passwordKey = rs.getString("passwordKey"),
            gender = rs.getObject("gender") as String?,
            age = rs.getObject("age") as Int?,
            height = rs.getObject("height") as Double?,
            weight = rs.getObject("weight") as Double?,
            accountType = rs.getBoolean("accountType")
            )
            rs.close()
            stmt.close()
            return user
    }
    rs.close()
    stmt.close()
    return null
}

fun updateAccount(
    email: String,
    gender: String?,
    age: Int?,
    height: Double?,
    weight: Double?
): Boolean {

    // Connect to db, pepare query struct, use object that allows updating
    // and prevent SQL injection, store to set values and exec
    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE users
        SET gender = ?, age = ?, height = ?, weight = ?
        WHERE email = ?
        """.trimIndent()
    )

    // Set placeholder values based on given data
    stmt.setString(1, gender)
    stmt.setObject(2, age)
    stmt.setObject(3, height)
    stmt.setObject(4, weight)
    stmt.setString(5, email)

    val rowsUpdated = stmt.executeUpdate() // smt.executeUpdate returns 0 if nothing updated and 1 if updated
    stmt.close()

    return rowsUpdated > 0 // Returns True is rowsUpdated == 1
}

fun deleteUser(email: String): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM users WHERE email = ?"
    )

    stmt.setString(1, email)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// Exercise db

fun createExercise(exercise: Exercise): Int {
    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        INSERT INTO exercises(name, category, description)
        VALUES (?, ?, ?)
        """.trimIndent(),
        Statement.RETURN_GENERATED_KEYS
    )

    stmt.setString(1, exercise.name)
    stmt.setString(2, exercise.category)
    stmt.setString(3, exercise.description)

    val rowsInserted = stmt.executeUpdate()

    if (rowsInserted == 0) {
        stmt.close()
        return -1
    }

    val rs = stmt.generatedKeys
    val id = if (rs.next()) rs.getInt(1) else -1

    rs.close()
    stmt.close()

    return id
}

fun findExercise(exerciseID: Int): Exercise? {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM exercises WHERE exerciseID = ?"
    )

    stmt.setInt(1, exerciseID)

    val rs: ResultSet = stmt.executeQuery()

    val exercise = if (rs.next()) {
        Exercise(
            exerciseID = rs.getInt("exerciseID"),
            name = rs.getString("name"),
            category = rs.getString("category"),
            description = rs.getString("description")
        )
    } else {
        null
    }

    rs.close()
    stmt.close()

    return exercise
}

fun findAllExercises(): List<Exercise> {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM exercises"
    )

    val rs: ResultSet = stmt.executeQuery()
    val exercises = mutableListOf<Exercise>()
    while (rs.next()) {
        val exercise = Exercise(
            exerciseID = rs.getInt("exerciseID"),
            name = rs.getString("name"),
            category = rs.getString("category"),
            description = rs.getString("description")
        )
        exercises.add(exercise)
    } 

    rs.close()
    stmt.close()

    return exercises
}

fun updateExercise(
    name: String,
    category: String,
    description: String?
): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE exercises
        SET category = ?, description = ?
        WHERE name = ?
        """.trimIndent()
    )

    stmt.setString(1, category)
    stmt.setString(2, description)
    stmt.setString(3, name)

    val rowsUpdated = stmt.executeUpdate()
    stmt.close()

    return rowsUpdated > 0
}

fun deleteExercise(exerciseID: Int): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM exercises WHERE exerciseID = ?"
    )

    stmt.setInt(1, exerciseID)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// Workout to DB
fun createWorkout(workout : Workout): Int {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        INSERT INTO workouts(createdBy, assignedTo, date, duration)
        VALUES (?, ?, ?, ?)
        """.trimIndent(),
        Statement.RETURN_GENERATED_KEYS
    )

    stmt.setInt(1, workout.createdBy)
    stmt.setObject(2, workout.assignedTo)
    stmt.setString(3, workout.date)
    stmt.setObject(4, workout.duration)

    val rowsInserted = stmt.executeUpdate()

    if (rowsInserted == 0) {
        stmt.close()
        return -1
    }

    val rs = stmt.generatedKeys
    val id = if (rs.next()) rs.getInt(1) else -1

    rs.close()
    stmt.close()

    return id
}

fun findAllWorkouts(userID: Int): List<Workout> {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM workouts WHERE assignedTo = ?"
    )

    stmt.setInt(1, userID)

    val rs: ResultSet = stmt.executeQuery()
    val workouts = mutableListOf<Workout>()

    while (rs.next()) {
        val workout = Workout(
            workoutID = rs.getInt("workoutID"),
            createdBy = rs.getInt("createdBy"),
            assignedTo = rs.getInt("assignedTo"),
            date = rs.getString("date"),
            duration = rs.getObject("duration") as Int?
        )
        workouts.add(workout)
    }

    rs.close()
    stmt.close()
    return workouts
}

fun updateWorkout(
    workoutID: Int,
    date: String,
    duration: Int?
): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE workouts
        SET date = ?, duration = ?
        WHERE workoutID = ?
        """.trimIndent()
    )

    stmt.setString(1, date)
    stmt.setObject(2, duration)
    stmt.setInt(3, workoutID)

    val rowsUpdated = stmt.executeUpdate()
    stmt.close()

    return rowsUpdated > 0
}

fun deleteWorkout(workoutID: Int): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM workouts WHERE workoutID = ?"
    )

    stmt.setInt(1, workoutID)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// Workout_exercise join table

fun addExerciseToWorkout(workout_exercise : WorkoutExercise): Int {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        INSERT INTO workout_exercises(workoutID, exerciseID, sets, reps, weight)
        VALUES (?, ?, ?, ?, ?)
        """.trimIndent(),
        Statement.RETURN_GENERATED_KEYS
    )

    stmt.setInt(1, workout_exercise.workoutID)
    stmt.setInt(2, workout_exercise.exerciseID)
    stmt.setObject(3, workout_exercise.sets)
    stmt.setObject(4, workout_exercise.reps)
    stmt.setObject(5, workout_exercise.weight)

    val rowsInserted = stmt.executeUpdate()

    if (rowsInserted == 0) {
        stmt.close()
        return -1
    }

    val rs = stmt.generatedKeys
    val id = if (rs.next()) rs.getInt(1) else -1

    rs.close()
    stmt.close()

    return id
}

fun findAllWorkoutExercises(workoutID: Int): List<WorkoutExercise> {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM workout_exercises WHERE workoutID = ?"
    )

    stmt.setInt(1, workoutID)

    val rs: ResultSet = stmt.executeQuery()
    val workoutExs = mutableListOf<WorkoutExercise>()
    while (rs.next()) {
        val we = WorkoutExercise(
            workoutExercisesID = rs.getInt("workoutExercisesID"),
            workoutID = rs.getInt("workoutID"),
            exerciseID = rs.getInt("exerciseID"),
            sets = rs.getObject("sets") as Int?,
            reps = rs.getObject("reps") as Int?,
            weight = rs.getObject("weight") as Double?
        )
        workoutExs.add(we)
    }

    rs.close()
    stmt.close()
    return workoutExs
}

fun updateWorkoutExercise(
    workoutExercisesID: Int,
    sets: Int?,
    reps: Int?,
    weight: Double?
): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE workout_exercises
        SET sets = ?, reps = ?, weight = ?
        WHERE workoutExercisesID = ?
        """.trimIndent()
    )

    stmt.setObject(1, sets)
    stmt.setObject(2, reps)
    stmt.setObject(3, weight)
    stmt.setInt(4, workoutExercisesID)

    val rowsUpdated = stmt.executeUpdate()
    stmt.close()

    return rowsUpdated > 0
}

fun deleteWorkoutExercise(workoutID: Int): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM workout_exercises WHERE workoutID = ?"
    )

    stmt.setInt(1, workoutID)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// Statistics DB
fun addStatistic(stat: Statistic): Int {

    val stmt = db.connection.prepareStatement(
        """
        INSERT INTO statistics(userID, exerciseID, scoreValue, date)
        VALUES (?, ?, ?, ?)
        """.trimIndent(),
        Statement.RETURN_GENERATED_KEYS
    )

    stmt.setInt(1, stat.userID)
    stmt.setInt(2, stat.exerciseID)
    stmt.setDouble(3, stat.scoreValue)
    stmt.setString(4, stat.date)

    val rowsInserted = stmt.executeUpdate()

    if (rowsInserted == 0) {
        stmt.close()
        return -1
    }

    val rs = stmt.generatedKeys
    val id = if (rs.next()) rs.getInt(1) else -1

    rs.close()
    stmt.close()

    return id
}

fun findAllStatistics(userID: Int, exerciseID: Int): List<Statistic> {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM statistics WHERE userID = ? AND exerciseID = ?"
    )

    stmt.setInt(1, userID)
    stmt.setInt(2, exerciseID)

    val rs: ResultSet = stmt.executeQuery()
    val stats = mutableListOf<Statistic>()
    while (rs.next()) {
        val stat = Statistic(
            statisticID = rs.getInt("statisticID"),
            userID = rs.getInt("userID"),
            exerciseID = rs.getInt("exerciseID"),
            scoreValue = rs.getDouble("scoreValue"),
            date = rs.getString("date")
        )
        stats.add(stat)
    }

    rs.close()
    stmt.close()
    return stats
}

fun getBestStat(userID: Int, exerciseID: Int): Statistic? {

    val stmt = db.connection.prepareStatement(
        """
        SELECT * FROM statistics
        WHERE userID = ? AND exerciseID = ?
        ORDER BY scoreValue DESC
        LIMIT 1
        """.trimIndent()
    )

    stmt.setInt(1, userID)
    stmt.setInt(2, exerciseID)

    val rs = stmt.executeQuery()

    val stat = if (rs.next()) {
        Statistic(
            rs.getInt("statisticID"),
            rs.getInt("userID"),
            rs.getInt("exerciseID"),
            rs.getDouble("scoreValue"),
            rs.getString("date")
        )
    } else null

    rs.close()
    stmt.close()

    return stat
}

fun updateStatistic(
    statisticID: Int,
    scoreValue: Double,
    date: String
): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE statistics
        SET scoreValue = ?, date = ?
        WHERE statisticID = ?
        """.trimIndent()
    )

    stmt.setDouble(1, scoreValue)
    stmt.setString(2, date)
    stmt.setInt(3, statisticID)

    val rowsUpdated = stmt.executeUpdate()
    stmt.close()

    return rowsUpdated > 0
}

fun deleteStatistic(statisticID: Int): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM statistics WHERE statisticID = ?"
    )

    stmt.setInt(1, statisticID)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// Competitions DB
fun addComp(comp: Competition): Int {

    val stmt = db.connection.prepareStatement(
        """
        INSERT INTO competitions(userID, name, date, rank)
        VALUES (?, ?, ?, ?)
        """.trimIndent(),
        Statement.RETURN_GENERATED_KEYS
    )

    stmt.setInt(1, comp.userID)
    stmt.setString(2, comp.name)
    stmt.setString(3, comp.date)
    stmt.setObject(4, comp.rank)

    val rowsInserted = stmt.executeUpdate()

    if (rowsInserted == 0) {
        stmt.close()
        return -1
    }

    val rs = stmt.generatedKeys
    val id = if (rs.next()) rs.getInt(1) else -1

    rs.close()
    stmt.close()

    return id
}

fun findAllCompetitions(userID: Int): List<Competition> {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM competitions WHERE userID = ?"
    )

    stmt.setInt(1, userID)

    val rs: ResultSet = stmt.executeQuery()
    val comps = mutableListOf<Competition>()
    while (rs.next()) {
        val comp = Competition(
            competitionID = rs.getInt("competitionID"),
            userID = rs.getInt("userID"),
            name = rs.getString("name"),
            date = rs.getString("date"),
            rank = rs.getObject("rank") as Int?
        )
        comps.add(comp)
    }

    rs.close()
    stmt.close()
    return comps
}

fun updateCompetition(
    competitionID: Int,
    name: String,
    date: String,
    rank: Int?
): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE competitions
        SET name = ?, date = ?, rank = ?
        WHERE competitionID = ?
        """.trimIndent()
    )

    stmt.setString(1, name)
    stmt.setString(2, date)
    stmt.setObject(3, rank)
    stmt.setInt(4, competitionID)

    val rowsUpdated = stmt.executeUpdate()
    stmt.close()

    return rowsUpdated > 0
}

fun deleteCompetition(competitionID: Int): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM competitions WHERE competitionID = ?"
    )

    stmt.setInt(1, competitionID)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// CompetitionsAndExercise DB
fun addCompExercise(compEx: CompetitionExercise): Int {

    val stmt = db.connection.prepareStatement(
        """
        INSERT INTO competition_exercises(competitionID, exerciseID, performance)
        VALUES (?, ?, ?)
        """.trimIndent(),
        Statement.RETURN_GENERATED_KEYS
    )

    stmt.setInt(1, compEx.competitionID)
    stmt.setInt(2, compEx.exerciseID)
    stmt.setObject(3, compEx.performance)

    val rowsInserted = stmt.executeUpdate()

    if (rowsInserted == 0) {
        stmt.close()
        return -1
    }

    val rs = stmt.generatedKeys
    val id = if (rs.next()) rs.getInt(1) else -1

    rs.close()
    stmt.close()

    return id
}

fun findAllCompetitionExercises(competitionID: Int): List<CompetitionExercise> {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM competition_exercises WHERE competitionID = ?"
    )

    stmt.setInt(1, competitionID)

    val rs: ResultSet = stmt.executeQuery()
    val compExs = mutableListOf<CompetitionExercise>()
    while (rs.next()) {
        val compEx = CompetitionExercise(
            competitionExerciseID = rs.getInt("competitionExerciseID"),
            competitionID = rs.getInt("competitionID"),
            exerciseID = rs.getInt("exerciseID"),
            performance = rs.getObject("performance") as Double?
        )
        compExs.add(compEx)
    }

    rs.close()
    stmt.close()
    return compExs
}

fun updateCompetitionExercise(
    competitionExerciseID: Int,
    performance: Double?
): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE competition_exercises
        SET performance = ?
        WHERE competitionExerciseID = ?
        """.trimIndent()
    )

    stmt.setObject(1, performance)
    stmt.setInt(2, competitionExerciseID)

    val rowsUpdated = stmt.executeUpdate()
    stmt.close()

    return rowsUpdated > 0
}

fun deleteCompetitionExercise(competitionExerciseID: Int): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM competition_exercises WHERE competitionExerciseID = ?"
    )

    stmt.setInt(1, competitionExerciseID)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// Routes DB

fun addRoute(route: Route): Int {

    val stmt = db.connection.prepareStatement(
        """
        INSERT INTO routes(userID, name, distance, estimatedTime, date)
        VALUES (?, ?, ?, ?, ?)
        """.trimIndent(),
        Statement.RETURN_GENERATED_KEYS
    )

    stmt.setInt(1, route.userID)
    stmt.setString(2, route.name)
    stmt.setDouble(3, route.distance)
    stmt.setObject(4, route.estimatedTime)
    stmt.setString(5, route.date)

    val rowsInserted = stmt.executeUpdate()

    if (rowsInserted == 0) {
        stmt.close()
        return -1
    }

    val rs = stmt.generatedKeys
    val id = if (rs.next()) rs.getInt(1) else -1

    rs.close()
    stmt.close()

    return id
}

fun findAllRoutes(userID: Int): List<Route> {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM routes WHERE userID = ?"
    )

    stmt.setInt(1, userID)

    val rs: ResultSet = stmt.executeQuery()
    val routes = mutableListOf<Route>()
    while (rs.next()) {
        val route = Route(
            routeID = rs.getInt("routeID"),
            userID = rs.getInt("userID"),
            name = rs.getString("name"),
            distance = rs.getDouble("distance"),
            estimatedTime = rs.getObject("estimatedTime") as Double?,
            date = rs.getString("date")
        )
        routes.add(route)
    }

    rs.close()
    stmt.close()
    return routes
}

fun updateRoute(
    routeID: Int,
    name: String,
    distance: Double,
    estimatedTime: Double?,
    date: String?
): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE routes
        SET name = ?, distance = ?, estimatedTime = ?, date = ?
        WHERE routeID = ?
        """.trimIndent()
    )

    stmt.setString(1, name)
    stmt.setDouble(2, distance)
    stmt.setObject(3, estimatedTime)
    stmt.setString(4, date)
    stmt.setInt(5, routeID)

    val rowsUpdated = stmt.executeUpdate()
    stmt.close()

    return rowsUpdated > 0
}

fun deleteRoute(routeID: Int): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM routes WHERE routeID = ?"
    )

    stmt.setInt(1, routeID)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// Calendar DB

fun addCalendarEvent(event: CalendarEvent): Int {

    val stmt = db.connection.prepareStatement(
        """
        INSERT INTO calendar_events(userID,eventType,eventID,status,scheduledDate,name)
        VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent(),
        Statement.RETURN_GENERATED_KEYS
    )

    stmt.setInt(1, event.userID)
    stmt.setString(2, event.eventType)
    stmt.setInt(3, event.eventID)
    stmt.setString(4, event.status)
    stmt.setString(5, event.scheduledDate)
    stmt.setString(6, event.name)

    val rowsInserted = stmt.executeUpdate()

    if (rowsInserted == 0) {
        stmt.close()
        return -1
    }

    val rs = stmt.generatedKeys
    val id = if (rs.next()) rs.getInt(1) else -1

    rs.close()
    stmt.close()

    return id
}

fun findAllCalendarEvents(userID: Int, scheduledDate: String): List<CalendarEvent> {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM calendar_events WHERE userID = ? AND scheduledDate = ?"
    )

    stmt.setInt(1, userID)
    stmt.setString(2, scheduledDate)

    val rs: ResultSet = stmt.executeQuery()
    val events = mutableListOf<CalendarEvent>()
    while (rs.next()) {
        val event = CalendarEvent(
            calendarID = rs.getInt("calendarID"),
            userID = rs.getInt("userID"),
            eventType = rs.getString("eventType"),
            eventID = rs.getInt("eventID"),
            status = rs.getString("status"),
            scheduledDate = rs.getString("scheduledDate"),
            name = rs.getString("name")
        )
        events.add(event)
    }

    rs.close()
    stmt.close()
    return events
}

fun updateCalendarEvent(
    calendarID: Int,
    status: String,
    scheduledDate: String,
    name: String
): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        """
        UPDATE calendar_events
        SET status = ?, scheduledDate = ?, name = ?
        WHERE calendarID = ?
        """.trimIndent()
    )

    stmt.setString(1, status)
    stmt.setString(2, scheduledDate)
    stmt.setString(3, name)
    stmt.setInt(4, calendarID)

    val rowsUpdated = stmt.executeUpdate()
    stmt.close()

    return rowsUpdated > 0
}

fun deleteCalendarEvent(calendarID: Int): Boolean {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "DELETE FROM calendar_events WHERE calendarID = ?"
    )

    stmt.setInt(1, calendarID)

    val rowsDeleted = stmt.executeUpdate()
    stmt.close()

    return rowsDeleted > 0
}

// Individual find functions

fun findRoute(routeID: Int): Route? {

    val stmt = db.connection.prepareStatement(
        "SELECT * FROM routes WHERE routeID = ?"
    )

    stmt.setInt(1, routeID)

    val rs = stmt.executeQuery()

    val route = if (rs.next()) {
        Route(
            routeID = rs.getInt("routeID"),
            userID = rs.getInt("userID"),
            name = rs.getString("name"),
            distance = rs.getDouble("distance"),
            estimatedTime = rs.getObject("estimatedTime") as Double?,
            date = rs.getString("date")
        )
    } else null

    rs.close()
    stmt.close()

    return route
}

fun findCalendarEvent(calendarID: Int): CalendarEvent? {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM calendar_events WHERE calendarID = ?"
    )

    stmt.setInt(1, calendarID)

    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) {
        val event = CalendarEvent(
            calendarID = rs.getInt("calendarID"),
            userID = rs.getInt("userID"),
            eventType = rs.getString("eventType"),
            eventID = rs.getInt("eventID"),
            status = rs.getString("status"),
            scheduledDate = rs.getString("scheduledDate"),
            name = rs.getString("name")
        )
        rs.close()
        stmt.close()
        return event
    }

    rs.close()
    stmt.close()
    return null
}

fun findWorkout(workoutID: Int): Workout? {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM workouts WHERE workoutID = ?"
    )

    stmt.setInt(1, workoutID)

    val rs: ResultSet = stmt.executeQuery()

    if (rs.next()) {
        val workout = Workout(
            workoutID = rs.getInt("workoutID"),
            createdBy = rs.getInt("createdBy"),
            assignedTo = rs.getInt("assignedTo"),
            date = rs.getString("date"),
            duration = rs.getObject("duration") as Int?
        )
        rs.close()
        stmt.close()
        return workout
    }

    rs.close()
    stmt.close()
    return null
}

fun findWorkoutExercise(workoutExercisesID: Int): WorkoutExercise? {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM workout_exercises WHERE workoutExercisesID = ?"
    )

    stmt.setInt(1, workoutExercisesID)

    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) {
        val we = WorkoutExercise(
            workoutExercisesID = rs.getInt("workoutExercisesID"),
            workoutID = rs.getInt("workoutID"),
            exerciseID = rs.getInt("exerciseID"),
            sets = rs.getObject("sets") as Int?,
            reps = rs.getObject("reps") as Int?,
            weight = rs.getObject("weight") as Double?
        )
        rs.close()
        stmt.close()
        return we
    }

    rs.close()
    stmt.close()
    return null
}

fun findStatistic(statisticID: Int): Statistic? {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM statistics WHERE statisticID = ?"
    )

    stmt.setInt(1, statisticID)

    val rs: ResultSet = stmt.executeQuery()

    if (rs.next()) {
        val stat = Statistic(
            statisticID = rs.getInt("statisticID"),
            userID = rs.getInt("userID"),
            exerciseID = rs.getInt("exerciseID"),
            scoreValue = rs.getDouble("scoreValue"),
            date = rs.getString("date")
        )
        rs.close()
        stmt.close()
        return stat
    }

    rs.close()
    stmt.close()
    return null
}

fun findCompetition(competitionID: Int): Competition? {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM competitions WHERE competitionID = ?"
    )

    stmt.setInt(1, competitionID)

    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) {
        val comp = Competition(
            competitionID = rs.getInt("competitionID"),
            userID = rs.getInt("userID"),
            name = rs.getString("name"),
            date = rs.getString("date"),
            rank = rs.getObject("rank") as Int?
        )
         rs.close()
        stmt.close()
        return comp
    }

    rs.close()
    stmt.close()
    return null
}

fun findCompetitionExercise(competitionExerciseID: Int): CompetitionExercise? {

    val stmt: PreparedStatement = db.connection.prepareStatement(
        "SELECT * FROM competition_exercises WHERE competitionExerciseID = ?"
    )

    stmt.setInt(1, competitionExerciseID)

    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) {
        val compEx = CompetitionExercise(
            competitionExerciseID = rs.getInt("competitionExerciseID"),
            competitionID = rs.getInt("competitionID"),
            exerciseID = rs.getInt("exerciseID"),
            performance = rs.getObject("performance") as Double?
        )
        rs.close()
        stmt.close()
        return compEx
    }

    rs.close()
    stmt.close()
    return null
}