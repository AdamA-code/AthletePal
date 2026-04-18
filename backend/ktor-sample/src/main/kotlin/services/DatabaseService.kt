package services
import java.sql.Connection // Interface for database connection
import java.sql.DriverManager // To connect to the DB
import java.sql.PreparedStatement // Precompiled SQL statement
import java.sql.ResultSet // Represents results of SQL queries
import java.sql.Statement
import java.time.LocalDate
import kotlinx.serialization.Serializable // Helps routes
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.UUID // Univversal unique identifier for reset tokens

// data classes to finitely store inputs (and potentially outputs) to database

class DatabaseService{
    
    @Serializable
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

    @Serializable
    data class Exercise(
        val exerciseID: Int,
        val name: String,
        val category: String,
        val description: String?
    )

    @Serializable
    data class Workout(
        val workoutID: Int = 0,
        val createdBy: Int,
        val assignedTo: Int,
        val date: String,
        val duration: Int? = null,
        val workoutType: String? = null
    )

    @Serializable
    data class WorkoutExercise(
        val workoutExercisesID: Int,
        val workoutID: Int,
        val exerciseID: Int,
        val sets: Int?,
        val reps: Int?,
        val weight: Double?
    )

    @Serializable
    data class Statistic(
        val statisticID: Int? = null,
        val userID: Int,
        val exerciseID: Int,
        val scoreValue: Double,
        val date: String
    )

    @Serializable
    data class Competition(
        val competitionID: Int? = null,
        val userID: Int,
        val name: String,
        val date: String,
        val rank: Int? = null,
        val result: Int? = null
    )

    @Serializable
    data class CompetitionExercise(
        val competitionExerciseID: Int,
        val competitionID: Int,
        val exerciseID: Int,
        val performance: Double?
    )

    @Serializable
    data class Route(
        val routeID: Int? = null,
        val userID: Int,
        val name: String,
        val distance: Double,
        val estimatedTime: Double? = null,
        val date: String
    )

    @Serializable
    data class CalendarEvent(
        val calendarID: Int,
        val userID: Int,
        val eventType: String,
        val eventID: Int,
        val status: String,
        val scheduledDate: String,
        val name: String
    )

    @Serializable
    data class TrainingGroup(
        val groupID: Int,
        val createdBy: Int,
        val name: String,
        val description: String?,
        val goal: String?
    )

    @Serializable
    data class GroupMember(
        val memberID: Int,
        val groupID: Int,
        val userID: Int
    )

    @Serializable
    data class GroupSession(
        val sessionID: Int,
        val groupID: Int,
        val workoutID: Int,
        val scheduledDate: String
    )

    // Connect to DB
    class DatabaseConnection {
        //open connection to database
        val connection: Connection = DriverManager.getConnection("jdbc:sqlite:${System.getProperty("user.dir")}/src/main/resources/users.db")

        // Runs automatically on creation
        init {
            connection.createStatement().execute("PRAGMA foreign_keys = ON") // Enable deletion cascading
        }
    }
    val db = DatabaseConnection()
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

            // Set placeholder values based on given data
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

    // Voluntary change, not forgot password
    fun updatePassword(email: String, oldPassword: String, newPassword: String): Boolean {
        val user = findUser(email) ?: return false

        val authService = AuthService()

        // Verify old password is correct via reverse of func
        if (!authService.verifyPassword(oldPassword, user.passwordKey, user.passwordHash)) {
            return false
        }

        // Generate new salt and hash
        val newSalt = authService.generateSalt()
        val newHash = authService.hashPassword(newPassword, newSalt)

        val stmt = db.connection.prepareStatement("""
            UPDATE users
            SET passwordHash = ?, passwordKey = ?
            WHERE email = ?
        """.trimIndent()
        )
        stmt.setString(1, newHash)
        stmt.setString(2, newSalt)
        stmt.setString(3, email)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    // Forgot password (email token)
    fun generatePasswordResetToken(email: String): String? {
        val user = findUser(email) ?: return null

        // Generate a random unique token
        val token = UUID.randomUUID().toString()
        
        // Set expiry to 1 hour from now
        val expiry = LocalDateTime.now().plusHours(1).toString()

        // Store token in DB
        val stmt = db.connection.prepareStatement("""
            INSERT INTO password_reset_tokens (userID, token, expiry, used)
            VALUES (?, ?, ?, 0)
        """.trimIndent()
        )
        stmt.setInt(1, user.userID)
        stmt.setString(2, token)
        stmt.setString(3, expiry)

        val rows = stmt.executeUpdate()
        stmt.close()

        return if (rows > 0) {token} else null
    }

    fun resetPasswordWithToken(token: String, newPassword: String): Boolean {
        // FInd unused token
        val stmt = db.connection.prepareStatement("""
            SELECT * FROM password_reset_tokens
            WHERE token = ? AND used = 0
        """.trimIndent()
        )
        stmt.setString(1, token)
        
        val rs = stmt.executeQuery()

        // If no matching token found return false
        if (!rs.next()) {
            rs.close()
            stmt.close()
            return false
        }

        // Get expiry and userID
        val expiry = LocalDateTime.parse(rs.getString("expiry")) // Set to parse object
        val userID = rs.getInt("userID")

        rs.close()
        stmt.close()

        if (LocalDateTime.now().isAfter(expiry)) return false

        // Generate new salt and hash for new password
        val authService = AuthService()
        val newKey = authService.generateSalt()
        val newHash = authService.hashPassword(newPassword, newKey)

        // Update password in db
        val updateStmt = db.connection.prepareStatement("""
            UPDATE users
            SET passwordHash = ?, passwordKey = ?
            WHERE userID = ?
        """.trimIndent()
        )
        updateStmt.setString(1, newHash)
        updateStmt.setString(2, newKey)
        updateStmt.setInt(3, userID)

        val updatedPassword = updateStmt.executeUpdate()
        updateStmt.close()

        // Mark token as used (cant be used again)
        val usedTokenStmt = db.connection.prepareStatement("""
            UPDATE password_reset_tokens
            SET used = 1 WHERE token = ?
        """.trimIndent()
        )
        usedTokenStmt.setString(1, token)

        val tokenUsed = usedTokenStmt.executeUpdate()
        usedTokenStmt.close()

        return updatedPassword > 0 && tokenUsed > 0
    }

    fun deleteUser(email: String): Boolean {

        val stmt: PreparedStatement = db.connection.prepareStatement(
            """DELETE FROM users
            WHERE email = ?
            """.trimIndent()
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
            INSERT INTO workouts(createdBy, assignedTo, date, duration, workoutType)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS
        )

        stmt.setInt(1, workout.createdBy)
        stmt.setObject(2, workout.assignedTo)
        stmt.setString(3, workout.date)
        stmt.setObject(4, workout.duration)
        stmt.setObject(5, workout.workoutType)

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
                duration = rs.getObject("duration") as Int?,
                workoutType = rs.getObject("workoutType") as String?
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
        duration: Int?,
        workoutType: String?
    ): Boolean {

        val stmt: PreparedStatement = db.connection.prepareStatement(
            """
            UPDATE workouts
            SET date = ?, duration = ?, workoutType = ?
            WHERE workoutID = ?
            """.trimIndent()
        )

        stmt.setString(1, date)
        stmt.setObject(2, duration)
        stmt.setObject(3, workoutType)
        stmt.setInt(4, workoutID)

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
            INSERT INTO competitions(userID, name, date, rank, result)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS
        )

        stmt.setInt(1, comp.userID)
        stmt.setString(2, comp.name)
        stmt.setString(3, comp.date)
        stmt.setObject(4, comp.rank)
        stmt.setObject(5, comp.result)

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
                rank = rs.getObject("rank") as Int?,
                result = rs.getObject("result") as Int?
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
        rank: Int? = null,
        result: Int? = null
    ): Boolean {

        val stmt: PreparedStatement = db.connection.prepareStatement(
            """
            UPDATE competitions
            SET name = ?, date = ?, rank = ?, result = ?
            WHERE competitionID = ?
            """.trimIndent()
        )

        stmt.setString(1, name)
        stmt.setString(2, date)
        stmt.setObject(3, rank)
        stmt.setObject(4, result)
        stmt.setInt(5, competitionID)

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
                duration = rs.getObject("duration") as Int?,
                workoutType = rs.getObject("workoutType") as String?
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
                rank = rs.getObject("rank") as Int?,
                result = rs.getObject("result") as Int?
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

    
    // Training groups

    fun createGroup(group: TrainingGroup): Int {
        val stmt = db.connection.prepareStatement(
            """
            INSERT INTO training_groups(createdBy, name, description, goal)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS
        )
        stmt.setInt(1, group.createdBy)
        stmt.setString(2, group.name)
        stmt.setObject(3, group.description)
        stmt.setObject(4, group.goal)

        val rowsInserted = stmt.executeUpdate()
        if (rowsInserted == 0) { 
            stmt.close() 
            return -1 
        }

        val rs = stmt.generatedKeys
        val groupID = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        // Automatically add creator as first member
        if (groupID != -1) {
            joinGroup(groupID, group.createdBy)
        }

        return groupID
    }

    fun deleteGroup(groupID: Int): Boolean {
        val stmt = db.connection.prepareStatement(
            "DELETE FROM training_groups WHERE groupID = ?"
        )
        stmt.setInt(1, groupID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    fun findGroup(groupID: Int): TrainingGroup? {
        val stmt = db.connection.prepareStatement(
            "SELECT * FROM training_groups WHERE groupID = ?"
        )
        stmt.setInt(1, groupID)

        val rs = stmt.executeQuery()

        val group = if (rs.next()) {
            TrainingGroup(
                groupID = rs.getInt("groupID"),
                createdBy = rs.getInt("createdBy"),
                name = rs.getString("name"),
                description = rs.getObject("description") as String?,
                goal = rs.getObject("goal") as String?
            )
        } else null

        rs.close()
        stmt.close()

        return group
    }

    fun findAllGroups(userID: Int): List<TrainingGroup> {
        val stmt = db.connection.prepareStatement(
            """
            SELECT training_groups.* FROM training_groups
            JOIN group_members ON training_groups.groupID = group_members.groupID
            WHERE group_members.userID = ?
            """.trimIndent()
        )
        stmt.setInt(1, userID)

        val rs = stmt.executeQuery()
        val groups = mutableListOf<TrainingGroup>()

        while (rs.next()) {
            groups.add(
                TrainingGroup(
                    groupID = rs.getInt("groupID"),
                    createdBy = rs.getInt("createdBy"),
                    name = rs.getString("name"),
                    description = rs.getObject("description") as String?,
                    goal = rs.getObject("goal") as String?
                )
            )
        }
        rs.close()
        stmt.close()

        return groups
    }

    fun updateGroup(groupID: Int, name: String, description: String?, goal: String?): Boolean {
        val stmt = db.connection.prepareStatement(
            """
            UPDATE training_groups SET name = ?, description = ?, goal = ?
            WHERE groupID = ?
            """.trimIndent()
        )
        stmt.setString(1, name)
        stmt.setString(2, description)
        stmt.setObject(3, goal)
        stmt.setObject(4, groupID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    // Group members

    fun joinGroup(groupID: Int, userID: Int): Boolean {
        val stmt = db.connection.prepareStatement(
            "INSERT INTO group_members(groupID, userID) VALUES (?, ?)"
        )

        stmt.setInt(1, groupID)
        stmt.setInt(2, userID)

        val rows = stmt.executeUpdate()
        stmt.close()
        
        return rows > 0
    }

    fun leaveGroup(groupID: Int, userID: Int): Boolean {
        val stmt = db.connection.prepareStatement(
            "DELETE FROM group_members WHERE groupID = ? AND userID = ?"
        )
        stmt.setInt(1, groupID)
        stmt.setInt(2, userID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    fun getGroupMembers(groupID: Int): List<User> {
        val stmt = db.connection.prepareStatement(
            """
            SELECT users.* FROM users
            JOIN group_members ON users.userID = group_members.userID
            WHERE group_members.groupID = ?
            """.trimIndent()
        )
        stmt.setInt(1, groupID)

        val rs = stmt.executeQuery()
        val members = mutableListOf<User>()

        while (rs.next()) {
            members.add(
                User(
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
            )
        }

        rs.close()
        stmt.close()

        return members
    }

    // Group training sessions

    fun addGroupSession(session: GroupSession): Int {
        val stmt = db.connection.prepareStatement(
            """
            INSERT INTO group_sessions(groupID, workoutID, scheduledDate)
            VALUES (?, ?, ?)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS
        )
        stmt.setInt(1, session.groupID)
        stmt.setInt(2, session.workoutID)
        stmt.setString(3, session.scheduledDate)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) { 
            stmt.close()
            return -1 
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()){rs.getInt(1)} else -1

        rs.close()
        stmt.close()

        return id
    }

    fun getUpcomingGroupSessions(groupID: Int): List<GroupSession> {

        val today = LocalDate.now().toString()
        val stmt = db.connection.prepareStatement(
            """
            SELECT * FROM group_sessions
            WHERE groupID = ? AND scheduledDate >= ?
            ORDER BY scheduledDate ASC
            """.trimIndent()
        )

        stmt.setInt(1, groupID)
        stmt.setString(2, today)

        val rs = stmt.executeQuery()
        val sessions = mutableListOf<GroupSession>()

        while (rs.next()) {
            sessions.add(
                GroupSession(
                    sessionID = rs.getInt("sessionID"),
                    groupID = rs.getInt("groupID"),
                    workoutID = rs.getInt("workoutID"),
                    scheduledDate = rs.getString("scheduledDate")
                )
            )
        }
        rs.close()
        stmt.close()

        return sessions
    }

    fun getPastGroupSessions(groupID: Int): List<GroupSession> {
        val today = LocalDate.now().toString()
        val stmt = db.connection.prepareStatement(
            """
            SELECT * FROM group_sessions
            WHERE groupID = ? AND scheduledDate < ?
            ORDER BY scheduledDate DESC
            """.trimIndent()
        )
        stmt.setInt(1, groupID)
        stmt.setString(2, today)

        val rs = stmt.executeQuery()
        val sessions = mutableListOf<GroupSession>()

        while (rs.next()) {
            sessions.add(
                GroupSession(
                    sessionID = rs.getInt("sessionID"),
                    groupID = rs.getInt("groupID"),
                    workoutID = rs.getInt("workoutID"),
                    scheduledDate = rs.getString("scheduledDate")
                )
            )
        }
        rs.close()
        stmt.close()

        return sessions
    }

    fun deleteGroupSession(sessionID: Int): Boolean {
        val stmt = db.connection.prepareStatement(
            "DELETE FROM group_sessions WHERE sessionID = ?"
        )
        stmt.setInt(1, sessionID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    //Functions for the dashboard of the frontend

    fun getRecentWorkouts(email: String): List<Workout>{

        val user = findUser(email) ?: return emptyList()
        val results = mutableListOf<Workout>()

        val query = """
            SELECT * FROM workouts
            WHERE assignedTo = ?
            ORDER BY date DESC
            LIMIT 5
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Workout(
                    workoutID = rs.getInt("workoutID"),
                    createdBy = rs.getInt("createdBy"),
                    assignedTo = rs.getInt("assignedTo"),
                    date = rs.getString("date"),
                    duration = rs.getObject("duration") as Int?,
                    workoutType = rs.getObject("workoutType") as String?
                )
            )
        }
        rs.close()
        stmt.close()

        return results
    }

    // Frontend potentially want to split it into 3 most recent of each
    fun getRecentWorkoutsByType(userID: Int): Map<String, List<Workout>> {
        return findAllWorkouts(userID).filter { it.workoutType != null }.sortedByDescending { it.date }.groupBy { it.workoutType!! }.mapValues { (_, workouts) -> workouts.take(3) }
    }

    fun getTodaysWorkout(email: String, today: LocalDate = LocalDate.now()): List<Workout>{

        val user = findUser(email) ?: return emptyList()
        val results = mutableListOf<Workout>()

        val query = """
            SELECT * FROM workouts
            WHERE assignedTo = ? AND date = ?
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)
        stmt.setString(2, today.toString()) // Local date finds the dates and to string makes it able to be stored in YYYY-MM-DD

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Workout(
                    workoutID = rs.getInt("workoutID"),
                    createdBy = rs.getInt("createdBy"),
                    assignedTo = rs.getInt("assignedTo"),
                    date = rs.getString("date"),
                    duration = rs.getObject("duration") as Int?,
                    workoutType = rs.getObject("workoutType") as String?
                )
            )
        }
        rs.close()
        stmt.close()
        
        return results
    }
    
    fun getUpcomingCompetitions(email: String): List<Competition>{
        
        val user = findUser(email) ?: return emptyList()

        val results = mutableListOf<Competition>()

        val query = """
            SELECT * FROM competitions
            WHERE userID = ? AND date >= ?
            ORDER BY date ASC
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)
        stmt.setString(2, LocalDate.now().toString())

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Competition(
                    competitionID = rs.getInt("competitionID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    date = rs.getString("date"),
                    rank = rs.getObject("rank") as Int?,
                    result = rs.getObject("result") as Int?
                )
            )
        }
        rs.close()
        stmt.close()
        
        return results
    }

    fun getRecentRoutes(email: String): List<Route> {

        val user = findUser(email) ?: return emptyList()

        val results = mutableListOf<Route>()

        val query = """
            SELECT * FROM routes
            WHERE userID = ?
            ORDER BY date DESC
            LIMIT 5
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Route(
                    routeID = rs.getInt("routeID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    distance = rs.getDouble("distance"),
                    estimatedTime = rs.getObject("estimatedTime") as Double?,
                    date = rs.getString("date")
                )
            )
        }
        rs.close()
        stmt.close()
        
        return results
    }

    fun getRecentCompetitions(email: String): List<Competition> {

        val user = findUser(email) ?: return emptyList()

        val results = mutableListOf<Competition>()

        val query = """
            SELECT * FROM competitions
            WHERE userID = ?
            ORDER BY date DESC
            LIMIT 5
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Competition(
                    competitionID = rs.getInt("competitionID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    date = rs.getString("date"),
                    rank = rs.getObject("rank") as Int?,
                    result = rs.getObject("result") as Int?
                )
            )
        }
        rs.close()
        stmt.close()
        
        return results
    }

    // Functions for the stats page
    fun getWorkoutProgression(userID: Int): Map<String, List<Workout>> {
        val today = LocalDate.now().toString()
        return findAllWorkouts(userID).filter { it.workoutType != null && it.date < today}.sortedBy { it.date }.groupBy { it.workoutType!! }
    }

    fun getTotalWorkoutsCompleted(userID: Int): Int {
        val today = LocalDate.now().toString()
        return findAllWorkouts(userID).count { it.date < today }
    }

    // Hours trained
    fun getHoursTrained(userID: Int): Double {
        return findAllWorkouts(userID).filter { it.date < LocalDate.now().toString() && it.duration != null }.sumOf { it.duration ?: 0 }.div(60.0)
    }

    // Workout Streak
    fun getCurrentStreak(userID: Int): Int {
        val today = LocalDate.now()
        val completed = findAllWorkouts(userID).filter { it.date < today.toString() }.map { LocalDate.parse(it.date) }.toSortedSet().reversed()

        var streak = 0
        var current = today.minusDays(1)

        for (date in completed) {
            if (date == current) {
                streak++
                current = current.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    // Weekly training history
    fun getWeeklyTrainingHistory(userID: Int): Map<String, Map<String, Any>> {
        val today = LocalDate.now()
        return findAllWorkouts(userID).filter { it.date < today.toString() && it.duration != null }
            .groupBy { workout ->
                val date = LocalDate.parse(workout.date)
                val weekStart = date.with(DayOfWeek.MONDAY).toString()
                weekStart
            }
            .mapValues { (_, workouts) ->
                mapOf(
                    "sessions" to workouts.size,
                    "totalDuration" to workouts.sumOf { it.duration ?: 0 }.div(60.0)
                )
            }
            .toSortedMap()
    }

    // Exercise by category
    fun getExerciseBreakdown(userID: Int): Map<String, Map<String, Any>> {
        val workouts = findAllWorkouts(userID).filter { it.date < LocalDate.now().toString() }
        val workoutIDs: List<Int> = workouts.mapNotNull { it.workoutID }
        
        if (workoutIDs.isEmpty()) return emptyMap()

        val categoryCounts = mutableMapOf<String, Int>()
        
        workoutIDs.forEach { workoutID ->
            val stmt = db.connection.prepareStatement("""
                SELECT exercises.category FROM workout_exercises
                JOIN exercises ON workout_exercises.exerciseID = exercises.exerciseID
                WHERE workout_exercises.workoutID = ?
            """)

            // Joins tables (above)
            stmt.setInt(1, workoutID)
            val rs = stmt.executeQuery()
            // Determines how many categories there are and which fall into which category
            while (rs.next()) {
                val category = rs.getString("category")
                categoryCounts[category] = (categoryCounts[category] ?: 0) + 1
            }
            rs.close()
            stmt.close()
        }

        //Total number
        val total = categoryCounts.values.sum().toDouble()

        //Returns counts and percentage for each category
        return categoryCounts.mapValues { (_, count) ->
            mapOf(
                "sessions" to count,
                "percentage" to ((count / total) * 100).toInt()
            )
        }
    }

    // For personal bests
    fun getPersonalBests(userID: Int): List<Statistic> {
        return findAllExercises().mapNotNull { exercise ->getBestStat(userID, exercise.exerciseID)}
    }

    // Log workout for workout logs page
    fun logWorkout(
        workout: Workout,
        exercises: List<WorkoutExercise>
    ): Int {
        // Create workout
        val workoutID = createWorkout(workout)
        if (workoutID == -1) return -1

        // Link each exercise to the workout
        exercises.forEach { exercise ->
            val we = WorkoutExercise(
                workoutExercisesID = 0,
                workoutID = workoutID,
                exerciseID = exercise.exerciseID,
                sets = exercise.sets,
                reps = exercise.reps,
                weight = exercise.weight
            )
            addExerciseToWorkout(we)
        }

        return workoutID
    }

    // Update a logged workout (in case of a changed exercise or another detail)
    fun updateLoggedWorkout(workout: Workout, exercises: List<WorkoutExercise>): Boolean {

        updateWorkout(workout.workoutID ?: return false, workout.date, workout.duration, workout.workoutType)

        // Delete all exercise links
        deleteWorkoutExercise(workout.workoutID)

        // Add new exercise list
        exercises.forEach { exercise ->addExerciseToWorkout(exercise.copy(workoutID = workout.workoutID))}
        return true
    }
}