package services
import java.sql.Connection // Interface for database connection
import java.sql.DriverManager // To connect to the DB
import java.sql.PreparedStatement // Precompiled SQL statement
import java.sql.ResultSet // Represents results of SQL queries

// data classes to finitely store inputs (and potentially outputs) to database

enum class GenderType {
    MALE,
    FEMALE,
    OTHER
}

data class User(
    val userID: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    val passwordKey: String,
    val gender: GenderType?,
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
    val createdBy: Int,
    val assignedTo: Int,
    val date: String,
    val duration: Int?
)

data class WorkoutExercise(
    val id: Int,
    val workoutID: Int,
    val exerciseID: Int,
    val sets: Int?,
    val reps: Int?,
    val weight: Double?,
)

data class Statistic(
    val statisticsID: Int,
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
    val id: Int,
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

enum class EventType {
    WORKOUT,
    ROUTE,
    COMPETITION
}

enum class EventStatus {
    UPCOMING,
    COMPLETED,
    TODAY,
}

data class CalendarEvent(
    val calendarID: Int,
    val userID: Int,
    val eventType: EventType,
    val eventID: Int,
    val status: EventStatus,
    val scheduledDate: String,
    val name: String
)

// Connect to DB
private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:${System.getProperty("user.dir")}/src/main/resources/users.db")

// User db

fun createUser(user: User): Boolean {
    if (findUser(user.email) == null) {
        // Connect to db, pepare query struct, use object that allows updating
        // and prevent SQL injection, store to set values and exec
        // SQL INSERT statement with "?"" as a placeholder
        val stmt: PreparedStatement = connection.prepareStatement(
            """
            INSERT INTO users(name, email, passwordHash, passwordKey, gender, age, height, weight, accountType)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        )

        // Set the values for each placeholder in the SQL statement
        stmt.setString(1, user.name)
        stmt.setString(2, user.email)
        stmt.setString(3, user.passwordHash)
        stmt.setString(4, user.passwordKey)
        stmt.setString(5, user.gender?.name)
        stmt.setObject(6, user.age)
        stmt.setObject(7, user.height)
        stmt.setObject(8, user.weight)
        stmt.setBoolean(9, user.accountType)

        stmt.executeUpdate() // Execute the INSERT command
        stmt.close() // Close the statement
        return true
    } else {
        return false
    }

}

fun findUser(email: String): User? {
    // Connect to db, pepare query struct, use object that allows updating
    // and prevent SQL injection, store to set values and exec
    val stmt: PreparedStatement = connection.prepareStatement(
        "SELECT * FROM users WHERE email = ?"
    )

    stmt.setString(1, email) // Put email value in placeholder (?)

    val rs: ResultSet = stmt.executeQuery()

    // If query returns a row, create a User object
    if (rs.next()) {
        val genderStr = rs.getString("gender") // If inside it breaks as could be a null or enum, db stores string, kotlin wants enum
        val user = User(
            userID = rs.getInt("userID"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            passwordHash = rs.getString("passwordHash"),
            passwordKey = rs.getString("passwordKey"),
            gender = if (genderStr != null) {
                GenderType.valueOf(genderStr)
            } else {
                null
            },
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
    val stmt: PreparedStatement = connection.prepareStatement(
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

// Exercise db

fun createExercise(exercise: Exercise): Boolean {
    if (findExercise(exercise.name) == null) {

        val stmt: PreparedStatement = connection.prepareStatement(
            """
            INSERT INTO exercises(name, category, description)
            VALUES (?, ?, ?)
            """.trimIndent()
        )

        stmt.setString(1, exercise.name)
        stmt.setString(2, exercise.category)
        stmt.setString(3, exercise.description)

        val rowsInserted = stmt.executeUpdate()
        stmt.close()
        return rowsInserted > 0
    } else{
        return false
    }
}

fun findExercise(name: String): Exercise? {

    val stmt: PreparedStatement = connection.prepareStatement(
        "SELECT * FROM exercises WHERE name = ?"
    )

    stmt.setString(1, name)

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

// Workout to DB
fun createWorkout(workout : Workout): Boolean {

    val stmt: PreparedStatement = connection.prepareStatement(
        """
        INSERT INTO workouts(createdBy, assignedTo, date, duration)
        VALUES (?, ?, ?, ?)
        """.trimIndent()
    )

    stmt.setInt(1, workout.createdBy)
    stmt.setObject(2, workout.assignedTo)
    stmt.setString(3, workout.date)
    stmt.setObject(4, workout.duration)

    val rowsInserted = stmt.executeUpdate()
    stmt.close()

    return rowsInserted > 0
}

// Workout_exercise join table

fun addExerciseToWorkout(workout_exercise : WorkoutExercise
): Boolean {

    val stmt: PreparedStatement = connection.prepareStatement(
        """
        INSERT INTO workout_exercises(workoutID, exerciseID, sets, reps, weight)
        VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()
    )

    stmt.setInt(1, workout_exercise.id)
    stmt.setInt(2, workout_exercise.workoutID)
    stmt.setInt(3, workout_exercise.exerciseID)
    stmt.setObject(4, workout_exercise.sets)
    stmt.setObject(5, workout_exercise.reps)
    stmt.setObject(6, workout_exercise.weight)

    val rowsInserted = stmt.executeUpdate()
    stmt.close()

    return rowsInserted > 0
}

// Statistics DB
fun addStatistic(stat: Statistic): Boolean {

    val stmt = connection.prepareStatement(
        """
        INSERT INTO statistics(userID, exerciseID, scoreValue, date)
        VALUES (?, ?, ?, ?)
        """.trimIndent()
    )

    stmt.setInt(1, stat.userID)
    stmt.setInt(2, stat.exerciseID)
    stmt.setDouble(3, stat.scoreValue)
    stmt.setString(4, stat.date)

    val rowsInserted = stmt.executeUpdate()
    stmt.close()

    return rowsInserted > 0
}

fun getBestStat(userID: Int, exerciseID: Int, type: String): Statistic? {

    val stmt = connection.prepareStatement(
        """
        SELECT * FROM statistics
        WHERE userID = ? AND exerciseID = ? AND type = ?
        ORDER BY value DESC
        LIMIT 1
        """.trimIndent()
    )

    stmt.setInt(1, userID)
    stmt.setInt(2, exerciseID)
    stmt.setString(3, type)

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

// Competitions DB
fun addComp(comp: Competition): Boolean {

    val stmt = connection.prepareStatement(
        """
        INSERT INTO competitions(competitionID, userID, name, date, rank)
        VALUES (?, ?, ?, ?, ?)
        """.trimIndent()
    )

    stmt.setInt(1, comp.competitionID)
    stmt.setInt(2, comp.userID)
    stmt.setString(3, comp.name)
    stmt.setString(4, comp.date)
    stmt.setObject(5, comp.rank)

    val rowsInserted = stmt.executeUpdate()
    stmt.close()

    return rowsInserted > 0
}

// CompetitionsAndExercise DB
fun addCompExercise(compEx: CompetitionExercise): Boolean {

    val stmt = connection.prepareStatement(
        """
        INSERT INTO competition_exercises(competitionID, exerciseID, performance)
        VALUES (?, ?, ?)
        """.trimIndent()
    )

    stmt.setInt(1, compEx.id)
    stmt.setInt(2, compEx.competitionID)
    stmt.setInt(3, compEx.exerciseID)
    stmt.setObject(4, compEx.performance)

    val rowsInserted = stmt.executeUpdate()
    stmt.close()

    return rowsInserted > 0
}

// Routes DB

fun addRoute(route: Route): Boolean {

    val stmt = connection.prepareStatement(
        """
        INSERT INTO routes(routeID, userID, name, distance, estimatedTime, date)
        VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()
    )

    stmt.setInt(1, route.routeID)
    stmt.setInt(2, route.userID)
    stmt.setString(3, route.name)
    stmt.setDouble(4, route.distance)
    stmt.setObject(5, route.estimatedTime)
    stmt.setString(6, route.date)

    val rowsInserted = stmt.executeUpdate()
    stmt.close()

    return rowsInserted > 0
}

// Calendar DB

fun addCalendarEvent(event: CalendarEvent): Boolean {

    val stmt = connection.prepareStatement(
        """
        INSERT INTO calendar_events(userID,eventType,eventID,status,scheduledDate,name)
        VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()
    )

    stmt.setInt(1, event.userID)
    stmt.setString(2, event.eventType.name) // Converts enum to string for storage
    stmt.setInt(3, event.eventID)
    stmt.setString(4, event.status.name)
    stmt.setString(5, event.scheduledDate)
    stmt.setString(6, event.name)

    val rowsInserted = stmt.executeUpdate()
    stmt.close()

    return rowsInserted > 0
}